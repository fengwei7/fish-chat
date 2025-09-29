package com.fish.chat.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class GroupDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String avatarUrl;
    private String notice;
    private List<GroupMemberDTO> members;
}