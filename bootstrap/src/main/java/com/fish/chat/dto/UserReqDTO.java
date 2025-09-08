package com.fish.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReqDTO {
    private Long id;
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String profile;
}
