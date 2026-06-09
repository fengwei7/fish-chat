package com.fish.chat.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友状态枚举
 * 
 * 对应数据库表 t_friend 的 status 字段
 */
@Getter
@AllArgsConstructor
public enum FriendStatus {
    
    /**
     * 待确认
     */
    PENDING(0, "待确认"),
    
    /**
     * 已确认（好友）
     */
    CONFIRMED(1, "已确认"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");
    
    /**
     * 状态值
     */
    private final Integer value;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据值获取枚举
     */
    public static FriendStatus fromValue(Integer value) {
        if (value == null) {
            return PENDING;
        }
        for (FriendStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return PENDING;
    }
    
    /**
     * 检查是否是好友关系
     */
    public boolean isFriend() {
        return this == CONFIRMED;
    }
}
