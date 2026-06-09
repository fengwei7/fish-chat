package com.fish.chat.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class BasePO implements Serializable {
    /**
     * ID（雪花算法自动生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * code
     */
    @TableField(fill = FieldFill.INSERT)
    private String code;

    /**
     * 状态：0-禁用 1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;
    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;

    /**
     * 逻辑删除标识：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
