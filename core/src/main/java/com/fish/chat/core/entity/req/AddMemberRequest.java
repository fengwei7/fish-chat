package com.fish.chat.core.entity.req;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AddMemberRequest {
    @NotBlank(message = "用户code不能为空")
    private String userCode;
}