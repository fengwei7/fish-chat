package com.fish.chat.bootstrap.config;

import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        // 对所有跨域预检Options请求放行
//        registry.addInterceptor(new HandlerInterceptor() {
//            @Override
//            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//                if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
//                    response.setStatus(HttpServletResponse.SC_OK);
//                    return false;
//                }
//                return true;
//            }
//        }).addPathPatterns("/**");

        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/login",
                    "/auth/register",
                    "/auth/logout",
                    "/error",
                    "/file/download/**",
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
    
    // 注意：不注册 SaServletFilter，只保留 SaInterceptor。
    // 原因是 SaServletFilter 在 Filter 层执行，会先于 Spring CORS 处理拦截 OPTIONS 预检请求，
    // 导致未登录时返回的 401 响应不带 CORS 头，浏览器报跨域错误。
    // SaInterceptor 在 Spring MVC Interceptor 层执行，CorsInterceptor 会先添加 CORS 头，
    // 因此无论认证成功或失败，响应都会包含正确的跨域头。
}
