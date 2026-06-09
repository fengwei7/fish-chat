package com.fish.chat.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员角色枚举（群组和频道通用）
 * 
 * 对应数据库表：
 * - t_group_member.role（群组）
 * - t_channel_member.role（频道）
 */
@Getter
@AllArgsConstructor
public enum MemberRole {
    
    /**
     * 普通成员/订阅者
     */
    MEMBER(0, "普通成员"),
    
    /**
     * 管理员
     */
    ADMIN(1, "管理员"),
    
    /**
     * 创建者/群主
     */
    OWNER(2, "创建者");
    
    /**
     * 角色值
     */
    private final Integer value;
    
    /**
     * 角色描述
     */
    private final String description;
    
    /**
     * 根据值获取枚举
     */
    public static MemberRole fromValue(Integer value) {
        if (value == null) {
            return MEMBER;
        }
        for (MemberRole role : values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return MEMBER;
    }
    
    /**
     * 检查是否有管理权限（管理员或创建者）
     */
    public boolean canManage() {
        return this == ADMIN || this == OWNER;
    }
    
    /**
     * 检查是否有发言权限（频道场景：仅管理员和创建者可发言）
     */
    public boolean canSpeak() {
        return this == ADMIN || this == OWNER;
    }
}
