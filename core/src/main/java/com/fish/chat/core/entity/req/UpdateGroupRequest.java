package com.fish.chat.core.entity.req;

import lombok.Data;

@Data
public class UpdateGroupRequest {
    private String name;
    private String avatar;
    private String notice;
}
