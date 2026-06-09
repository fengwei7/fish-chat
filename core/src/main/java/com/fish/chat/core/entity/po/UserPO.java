package com.fish.chat.core.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fish.chat.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_user")
public class UserPO extends BasePO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（BCrypt 加密，salt 已嵌入哈希值）
     */
    private String password;

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
}
