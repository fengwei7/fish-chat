package com.fish.chat.core.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息 DTO
 */
@Data
public class UserDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户code
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
     * 是否在线
     */
    private Boolean online;
}
