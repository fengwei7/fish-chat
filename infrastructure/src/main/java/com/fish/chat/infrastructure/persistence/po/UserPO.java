package com.fish.chat.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fish.chat.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户持久化对象（PO）
 * 
 * 职责：
 * - 数据库表映射
 * - 只包含数据字段，不包含业务逻辑
 * - 继承 BasePO 获得公共字段（id, code, status, createTime, updateTime, creator, updater, deleted）
 * 
 * 数据库表：t_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserPO extends BasePO {
    
    /**
     * 用户名（登录用）
     */
    private String username;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 昵称（显示用）
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
