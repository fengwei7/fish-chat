package com.fish.chat.dto;

import lombok.Data;

/**
 * WebSocket消息DTO
 */
@Data
public class WebSocketMessageDTO {
    
    /**
     * 消息类型 (如: chat, connect, ping, pong, error等)
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
    
    /**
     * 消息状态
     */
    private String status;
    
    /**
     * 错误信息（当type为error时使用）
     */
    private String errorMessage;
}