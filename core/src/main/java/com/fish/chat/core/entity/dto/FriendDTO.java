package com.fish.chat.core.entity.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class FriendDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String remark;
    private Integer status;
    private Boolean online;
}