package com.fish.chat.domain.base;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域实体基类
 * 
 * 所有领域实体继承此类，获得公共字段：
 * - id: 实体 ID
 * - code: 业务编码
 * - status: 状态
 * - createTime: 创建时间
 * - updateTime: 更新时间
 * - creator: 创建人
 * - updater: 更新人
 * - deleted: 逻辑删除标识
 */
@Data
public class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 实体 ID
     */
    private Long id;
    
    /**
     * 业务编码（唯一标识）
     */
    private String code;
    
    /**
     * 状态：0-禁用 1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String creator;
    
    /**
     * 更新人
     */
    private String updater;
    
    /**
     * 逻辑删除标识：0-未删除 1-已删除
     */
    private Integer deleted;
}
