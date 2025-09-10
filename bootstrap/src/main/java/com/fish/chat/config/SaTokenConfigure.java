package com.fish.chat.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                .match("/**")
                .notMatch(excludePaths())
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

    // 动态获取哪些 path 可以忽略鉴权
    public List<String> excludePaths() {
//        return Arrays.asList("/path1", "/path2", "/path3");
        return Arrays.asList("/auth/login","/auth/register");
    }
}
