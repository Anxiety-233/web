package com.lab.equipment.exception;

import com.lab.equipment.dto.ResultDTO;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ResultDTO<?> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return ResultDTO.error(e.getMessage());
    }

    // 权限不足
    @ExceptionHandler(AccessDeniedException.class)
    public ResultDTO<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足：", e);
        return ResultDTO.error("权限不足，无法访问");
    }

    // 认证失败（登录时用户名或密码错误）
    @ExceptionHandler(AuthenticationException.class)
    public ResultDTO<?> handleAuthenticationException(AuthenticationException e) {
        log.error("认证失败：", e);
        return ResultDTO.error("用户名或密码错误");
    }

    // JWT异常
    @ExceptionHandler(JwtException.class)
    public ResultDTO<?> handleJwtException(JwtException e) {
        log.error("Token异常：", e);
        return ResultDTO.error("Token无效：" + e.getMessage());
    }

    // 数据库异常
    @ExceptionHandler(DataAccessException.class)
    public ResultDTO<?> handleDataAccessException(DataAccessException e) {
        log.error("数据库异常：", e);
        return ResultDTO.error("数据库操作失败");
    }

    // 新增：处理请求参数校验异常（如LoginDTO的NotBlank）
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResultDTO<?> handleValidationException(Exception e) {
        log.error("参数校验异常：", e);
        String message;
        if (e instanceof MethodArgumentNotValidException) {
            message = ((MethodArgumentNotValidException) e).getBindingResult()
                    .getFieldErrors().stream()
                    .map(err -> err.getField() + ":" + err.getDefaultMessage())
                    .collect(Collectors.joining(";"));
        } else if (e instanceof BindException) {
            message = ((BindException) e).getBindingResult()
                    .getFieldErrors().stream()
                    .map(err -> err.getField() + ":" + err.getDefaultMessage())
                    .collect(Collectors.joining(";"));
        } else {
            message = "参数校验失败";
        }
        return ResultDTO.error(message);
    }

    // 新增：处理PathVariable/RequestParam校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    public ResultDTO<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数约束异常：", e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(";"));
        return ResultDTO.error(message);
    }

    // 通用异常（兜底）
    @ExceptionHandler(Exception.class)
    public ResultDTO<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return ResultDTO.error("系统异常，请联系管理员");
    }
}