package com.fish.chat.domain.user.model.entity;

import com.fish.chat.common.exception.DomainException;
import com.fish.chat.domain.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（BCrypt 加密）
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
    
    // ========== 业务行为 ==========
    
    /**
     * 更新用户资料
     * 
     * 业务规则:
     * - 昵称长度不能超过50个字符
     * - 邮箱格式必须正确
     * - 手机号格式必须正确
     */
    public void updateProfile(String nickname, String avatarUrl, String profile, String email, String mobile) {
        if (nickname != null && nickname.length() > 50) {
            throw new DomainException("昵称长度不能超过50个字符");
        }
        if (email != null && !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new DomainException("邮箱格式错误");
        }
        if (mobile != null && !mobile.matches("^1[3-9]\\d{9}$")) {
            throw new DomainException("手机号格式错误");
        }
        
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.profile = profile;
        this.email = email;
        this.mobile = mobile;
        this.setUpdateTime(LocalDateTime.now());
    }
    
    /**
     * 修改密码
     * 
     * 业务规则:
     * - 必须验证原密码
     * - 新密码长度不能少于6个字符
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (!BCrypt.checkpw(oldPassword, this.password)) {
            throw new DomainException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new DomainException("密码长度不能少于6个字符");
        }
        this.password = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        this.setUpdateTime(LocalDateTime.now());
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }
    
    /**
     * 创建新用户（工厂方法）
     * 
     * 业务规则:
     * - 用户名不能为空
     * - 密码长度不能少于6个字符
     * - 自动生成 code 和 createTime
     */
    public static User create(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new DomainException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new DomainException("密码长度不能少于6个字符");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }
}
