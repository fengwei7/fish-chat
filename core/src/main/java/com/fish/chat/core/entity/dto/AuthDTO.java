package com.fish.chat.core.entity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 认证响应 DTO
 */
@Data
@Builder
public class AuthDTO {
    
    /**
     * Token
     */
    private String token;
    
    /**
     * 用户 code
     */
    private String code;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像 URL
     */
    private String avatarUrl;
}
