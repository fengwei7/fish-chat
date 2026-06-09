package com.fish.chat.core.entity.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class GroupMemberDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Integer role;
    private Boolean online;
}
