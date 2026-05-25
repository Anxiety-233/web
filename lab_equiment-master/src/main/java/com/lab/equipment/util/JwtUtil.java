package com.lab.equipment.util;


import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类：生成/解析Token、黑名单管理
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret:default-secret-key-1234567890}") // 增加默认值，避免启动失败
    private String secret;

    /**
     * Access Token过期时间（毫秒）
     */
    @Value("${jwt.access-token-expire:3600000}") // 1小时默认值
    private long accessTokenExpire;

    /**
     * Refresh Token过期时间（毫秒）
     */
    @Value("${jwt.refresh-token-expire:86400000}") // 24小时默认值
    private long refreshTokenExpire;

    /**
     * Token黑名单Redis前缀
     */
    @Value("${jwt.blacklist-prefix:jwt:blacklist:}")
    private String blacklistPrefix;

    /**
     * Refresh Token Redis前缀
     */
    @Value("${jwt.refresh-token-prefix:jwt:refresh:}")
    private String refreshTokenPrefix;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 生成Access Token
     * @param userDetails 用户信息
     * @return Access Token
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpire);
    }

    /**
     * 生成Refresh Token
     * @param userDetails 用户信息
     * @return Refresh Token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        String refreshToken = generateToken(userDetails, refreshTokenExpire);
        // 将Refresh Token存入Redis（HttpOnly Cookie存储，Redis做备份）
        redisTemplate.opsForValue().set(
                refreshTokenPrefix + userDetails.getUsername(),
                refreshToken,
                refreshTokenExpire,
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    /**
     * 生成JWT Token通用方法
     * @param userDetails 用户信息
     * @param expire 过期时间
     * @return Token字符串
     */
    private String generateToken(UserDetails userDetails, long expire) {
        // 构建JWT payload
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities());

        // 修复：long转int溢出问题，改用Date构造器
        Date expireDate = new Date(System.currentTimeMillis() + expire);

        return Jwts.builder()
                .setClaims(claims)  // 设置自定义载荷
                .setSubject(userDetails.getUsername())  // 设置主题（用户名）
                .setIssuedAt(new Date())  // 设置签发时间
                .setExpiration(expireDate)  // 修复：替换DateUtil偏移，避免int溢出
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())  // HS256签名+密钥
                .compact();
    }

    /**
     * 解析Token获取用户名
     * @param token Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 解析Token获取载荷
     * @param token Token字符串
     * @return 载荷Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("JWT Token已过期", e);
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token已过期");
        } catch (UnsupportedJwtException e) {
            log.error("JWT Token格式不支持", e);
            throw new UnsupportedJwtException("Token格式不支持");
        } catch (MalformedJwtException e) {
            log.error("JWT Token格式错误", e);
            throw new MalformedJwtException("Token格式错误");
        } catch (SignatureException e) {
            log.error("JWT Token签名验证失败", e);
            throw new SignatureException("Token签名验证失败");
        } catch (IllegalArgumentException e) {
            log.error("JWT Token参数非法", e);
            throw new IllegalArgumentException("Token参数非法");
        } catch (Exception e) {
            log.error("解析JWT Token失败", e);
            throw new JwtException("Token解析失败", e);
        }
    }

    /**
     * 校验Token是否有效
     * @param token Token字符串
     * @param userDetails 用户信息
     * @return true-有效 false-无效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        // 1. 检查是否在黑名单
        if (isBlacklist(token)) {
            return false;
        }
        // 2. 检查用户名是否匹配
        String username = getUsernameFromToken(token);
        // 3. 检查是否过期
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * 检查Token是否过期
     * @param token Token字符串
     * @return true-过期 false-未过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * 将Token加入黑名单
     * @param token Token字符串
     */
    public void addToBlacklist(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            long expireTime = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (expireTime > 0) {
                // 黑名单过期时间 = Token剩余有效期
                redisTemplate.opsForValue().set(
                        blacklistPrefix + token,
                        "1",
                        expireTime,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.error("加入Token黑名单失败", e);
        }
    }

    /**
     * 检查Token是否在黑名单
     * @param token Token字符串
     * @return true-在黑名单 false-不在
     */
    public boolean isBlacklist(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistPrefix + token));
    }

    /**
     * 移除Refresh Token（登出时）
     * @param username 用户名
     */
    public void removeRefreshToken(String username) {
        redisTemplate.delete(refreshTokenPrefix + username);
    }

    /**
     * 验证Refresh Token有效性
     * @param username 用户名
     * @param refreshToken 待验证的Refresh Token
     * @return true-有效 false-无效
     */
    public boolean validateRefreshToken(String username, String refreshToken) {
        if (refreshToken == null || username == null) {
            return false;
        }
        String redisToken = redisTemplate.opsForValue().get(refreshTokenPrefix + username);
        return refreshToken.equals(redisToken) && !isTokenExpired(refreshToken);
    }
}
