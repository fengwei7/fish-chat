package com.fish.chat.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 * 
 * 适用于用户、群组、频道等实体的状态管理
 */
@Getter
@AllArgsConstructor
public enum CommonStatus {
    
    /**
     * 禁用/关闭/解散
     */
    DISABLED(0, "禁用"),
    
    /**
     * 正常/启用
     */
    NORMAL(1, "正常");
    
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
    public static CommonStatus fromValue(Integer value) {
        if (value == null) {
            return DISABLED;
        }
        for (CommonStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return DISABLED;
    }
    
    /**
     * 检查是否可用
     */
    public boolean isActive() {
        return this == NORMAL;
    }
}
