package com.fish.chat.bootstrap.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限配置
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    
    @Bean
    public SaTokenContext saTokenContext() {
        return new SaTokenContextForThreadLocal();
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/login",
                    "/auth/register",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/**/*.png",
                    "/**/*.jpg",
                    "/**/*.jpeg",
                    "/**/*.gif",
                    "/**/*.ico",
                    "/static/**",
                    "/webjars/**"
                );
    }
    
    @Bean
    public cn.dev33.satoken.filter.SaServletFilter saServletFilter() {
        return new cn.dev33.satoken.filter.SaServletFilter()
            .addInclude("/**")
            .addExclude(
                "/auth/login",
                "/auth/register",
                "/**/*.html",
                "/**/*.css",
                "/**/*.js",
                "/**/*.png",
                "/**/*.jpg",
                "/**/*.jpeg",
                "/**/*.gif",
                "/**/*.ico",
                "/static/**",
                "/webjars/**"
            );
    }
}
