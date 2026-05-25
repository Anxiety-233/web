package com.lab.equipment.aspect;

import cn.hutool.core.util.StrUtil;
import com.lab.equipment.entity.OperationLog;
import com.lab.equipment.service.OperationLogService;
import com.lab.equipment.util.BrowserUtil;
import com.lab.equipment.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private OperationLogService operationLogService;

    // 敏感参数关键字
    private static final List<String> SENSITIVE_KEYS = Arrays.asList("password", "pwd", "token", "refreshToken");
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile("(?i)(" + String.join("|", SENSITIVE_KEYS) + ")[\\s]*[:=][\\s]*[^,\\}]+");

    /**
     * 切点：所有Controller层方法
     */
    @Pointcut("execution(* com.lab.equipment.controller..*.*(..))")
    public void operationLogPointcut() {}

    /**
     * 环绕通知：记录操作日志
     */
    @Around("operationLogPointcut()")
    public Object recordOperationLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 2. 获取当前登录用户
        String username = "匿名用户";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
        }

        // 3. 构建日志对象
        OperationLog logEntity = new OperationLog();
        logEntity.setUsername(username);
        logEntity.setIp(IpUtil.getIpAddr(request));  // 获取IP
        logEntity.setBrowser(BrowserUtil.getBrowser(request));  // 获取浏览器信息
        logEntity.setTimestamp(Long.parseLong(StrUtil.blankToDefault(request.getHeader("timestamp"), "0")));
        logEntity.setNonce(request.getHeader("nonce"));
        // 获取操作行为（接口描述，这里简化为请求URL）
        logEntity.setOperation(request.getRequestURI());
        // 修复：过滤敏感请求参数（如密码）
        Object[] args = joinPoint.getArgs();
        String requestParams = Arrays.toString(args);
        logEntity.setRequestParams(filterSensitiveInfo(requestParams));

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            // 4. 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            log.error("操作日志记录失败：目标方法执行异常", e);
            throw e;
        } finally {
            // 5. 记录响应结果和操作时间（修正为LocalDateTime）
            logEntity.setResponseResult(StrUtil.toString(result));
            logEntity.setOperationTime(LocalDateTime.now());
            // 6. 保存日志到数据库
            operationLogService.save(logEntity);
            log.info("操作日志记录完成，耗时：{}ms", System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 过滤敏感信息（如密码、Token）
     */
    private String filterSensitiveInfo(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        // 替换敏感参数值为****
        return SENSITIVE_PATTERN.matcher(content).replaceAll("$1=****");
    }
}