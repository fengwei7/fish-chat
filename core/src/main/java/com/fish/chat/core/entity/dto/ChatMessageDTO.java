package com.fish.chat.core.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 聊天消息 DTO（含发送者昵称和头像）
 */
@Data
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String type;
    private String from;
    private String senderName;
    private String senderAvatar;
    private String to;
    private String content;
    private Long timestamp;
    private String roomCode;
    private String roomType;
    private String fileName;
    private Long fileSize;
}
