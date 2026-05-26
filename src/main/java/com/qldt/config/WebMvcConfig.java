package com.qldt.config;

import com.qldt.interceptor.UserContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                // Áp dụng cho tất cả request trừ static resources
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/webjars/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/login",
                        "/logout",
                        "/error"
                );
    }
}