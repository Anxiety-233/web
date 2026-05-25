package com.lab.equipment.interceptor;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AntiReplayInterceptor implements HandlerInterceptor {

    /**
     * 防重放过期时间（毫秒）- 默认5分钟
     */
    @Value("${anti-replay.expire:300000}")
    private long antiReplayExpire;

    /**
     * Nonce Redis前缀 - 默认值
     */
    @Value("${anti-replay.nonce-prefix:anti_replay:nonce:}")
    private String noncePrefix;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        // 1. 获取请求头中的timestamp和nonce
        String timestampStr = request.getHeader("timestamp");
        String nonce = request.getHeader("nonce");

        // 2. 校验参数是否为空
        if (StrUtil.isBlank(timestampStr) || StrUtil.isBlank(nonce)) {
            log.error("防重放校验失败：timestamp或nonce为空");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请求参数缺失：timestamp/nonce");
            return false;
        }

        // 3. 校验timestamp是否在有效期内
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            log.error("防重放校验失败：timestamp格式错误", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("timestamp格式错误，必须为数字");
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - timestamp) > antiReplayExpire) {
            log.error("防重放校验失败：timestamp过期，当前时间：{}，请求时间：{}", currentTime, timestamp);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请求已过期（5分钟内有效）");
            return false;
        }

        // 4. 校验nonce是否已被使用（Redis SETNX：不存在则设置，存在则返回false）
        String nonceKey = noncePrefix + nonce;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(nonceKey, "1", antiReplayExpire, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(isNew)) {
            log.error("防重放校验失败：nonce已被使用，nonce：{}", nonce);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("请求重复（nonce已使用）");
            return false;
        }

        // 5. 校验通过
        return true;
    }
}