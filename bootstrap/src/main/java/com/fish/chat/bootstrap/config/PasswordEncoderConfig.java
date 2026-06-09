package com.fish.chat.bootstrap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置
 */
@Configuration
public class PasswordEncoderConfig {
    
    /**
     * BCrypt 强度配置（范围 4-31，默认 10）
     * 数值越大计算越慢，安全性越高
     */
    @Value("${fish-chat-config.security.password.bcrypt-strength:10}")
    private int bcryptStrength;
    
    /**
     * BCrypt 密码编码器 Bean
     * 
     * BCrypt 优势：
     * 1. 自动加盐，salt 嵌入到哈希值中
     * 2. 计算慢，防暴力破解
     * 3. 可调强度，适应硬件升级
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
