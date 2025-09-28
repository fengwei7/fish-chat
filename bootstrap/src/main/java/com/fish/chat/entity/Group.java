package com.fish.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_group")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 群组名称
     */
    @TableField("name")
    private String name;

    /**
     * 群组描述
     */
    @TableField("description")
    private String description;

    /**
     * 群主ID
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 群组头像
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 群组公告
     */
    @TableField("notice")
    private String notice;
    
    /**
     * 群组最大成员数
     */
    @TableField("max_members")
    private Integer maxMembers = 1000;

    /**
     * 群组类型：1-普通群 2-公开群
     */
    @TableField("type")
    private Integer type = 1;

    /**
     * 入群验证方式：0-无需验证 1-需要验证 2-不允许加群
     */
    @TableField("join_permission")
    private Integer joinPermission = 1;

    /**
     * 群组状态：0-解散 1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标识：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}