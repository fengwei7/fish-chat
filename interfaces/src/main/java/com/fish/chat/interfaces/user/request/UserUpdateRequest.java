package com.fish.chat.interfaces.user.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest {
    
    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
    
    /**
     * 头像 URL
     */
    private String avatarUrl;
    
    /**
     * 个人简介
     */
    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String profile;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;
}
