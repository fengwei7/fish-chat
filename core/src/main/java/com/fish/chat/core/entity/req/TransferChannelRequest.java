package com.fish.chat.core.entity.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 频道转让请求
 */
@Data
public class TransferChannelRequest {
    
    /**
     * 新创建者的用户code
     */
    @NotBlank(message = "新创建者不能为空")
    private String newOwnerCode;
}
