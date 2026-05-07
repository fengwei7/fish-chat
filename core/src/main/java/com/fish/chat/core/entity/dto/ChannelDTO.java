package com.fish.chat.core.entity.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ChannelDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String avatar;
    private String ownerCode;
    private String description;
    private Integer subscriberCount;
    private Integer status;
}