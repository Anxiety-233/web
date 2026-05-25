package com.lab.equipment.config;

import com.lab.equipment.interceptor.AntiReplayInterceptor;
import com.lab.equipment.interceptor.HorizontalPrivilegeInterceptor;
import com.lab.equipment.interceptor.UnauthorizedInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AntiReplayInterceptor antiReplayInterceptor;

    @Resource
    private HorizontalPrivilegeInterceptor horizontalPrivilegeInterceptor;

    @Resource
    private UnauthorizedInterceptor unauthorizedInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 防重放拦截
        registry.addInterceptor(antiReplayInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/refresh-token");

        // 未授权拦截
        registry.addInterceptor(unauthorizedInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/refresh-token");

        // 水平越权拦截
        registry.addInterceptor(horizontalPrivilegeInterceptor)
                .addPathPatterns("/equipment/**");
    }
}