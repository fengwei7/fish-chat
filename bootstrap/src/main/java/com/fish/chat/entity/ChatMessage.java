package com.fish.chat.entity;

import lombok.Data;

/**
 * 聊天消息
 */
@Data
public class ChatMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 发送方用户ID
     */
    private String from;

    /**
     * 接收方用户ID
     */
    private String to;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 时间戳
     */
    private Long timestamp;
}