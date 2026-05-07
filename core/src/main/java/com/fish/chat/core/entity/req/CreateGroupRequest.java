package com.fish.chat.core.entity.req;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "群名称不能为空")
    private String name;
    private String avatar;
}