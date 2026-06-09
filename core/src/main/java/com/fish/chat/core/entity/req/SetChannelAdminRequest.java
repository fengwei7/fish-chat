package com.fish.chat.core.entity.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 设置频道管理员请求
 */
@Data
public class SetChannelAdminRequest {
    
    /**
     * 是否设置为管理员
     * true=设置为管理员，false=取消管理员
     */
    @NotNull(message = "操作类型不能为空")
    private Boolean isAdmin;
}
