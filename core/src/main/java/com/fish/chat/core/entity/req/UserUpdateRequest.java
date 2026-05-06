package com.fish.chat.core.entity.req;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {

    @Size(min = 1, max = 30, message = "昵称长度必须在 1-30 之间")
    private String nickname;

    @Size(max = 100, message = "头像URL长度不能超过100")
    private String avatarUrl;

    @Size(max = 255, message = "个人简介长度不能超过255")
    private String profile;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String mobile;
}
