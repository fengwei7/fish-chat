package com.fish.chat.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class GroupMemberDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long groupId;
    private Long userId;
    private String nickname;
    private Integer role;
    private String username;
    private String userAvatar;
}