package com.fish.chat.application.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO {
    
    /**
     * 用户唯一标识
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
    
    /**
     * 个人简介
     */
    private String profile;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String mobile;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
