package com.fish.chat.core.handler;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * WebSocket 消息处理器接口
 */
public interface MessageHandler {
    
    /**
     * 获取支持的消息类型
     * @return 消息类型
     */
    String getType();
    
    /**
     * 处理消息
     * @param session WebSocket 会话
     * @param payload 消息内容
     */
    void handle(WebSocketSession session, Map<String, Object> payload);
}
