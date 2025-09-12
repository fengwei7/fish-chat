package com.fish.chat.service.impl;

import com.alibaba.fastjson.JSON;
import com.fish.chat.service.ChatWebSocketService;
import com.fish.chat.websocket.handler.ChatWebSocketHandler;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket服务实现类
 */
@Service
public class ChatWebSocketServiceImpl implements ChatWebSocketService {

    /**
     * 发送消息给指定用户
     *
     * @param userId 用户ID
     * @param type 消息类型
     * @param message 消息内容
     */
    @Override
    public void sendMessageToUser(String userId, String type, String message) {
        WebSocketSession session = ChatWebSocketHandler.getOnlineSessions().get(userId);
        if (session != null && session.isOpen()) {
            try {
                // 构造消息
                String jsonMessage = buildMessage(type, message);
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (Exception e) {
                System.err.println("发送消息给用户 " + userId + " 失败: " + e.getMessage());
            }
        } else {
            System.out.println("用户 " + userId + " 不在线，无法发送消息");
        }
    }

    /**
     * 发送消息给所有在线用户
     *
     * @param type 消息类型
     * @param message 消息内容
     */
    @Override
    public void broadcastMessage(String type, String message) {
        // 构造消息
        String jsonMessage = buildMessage(type, message);

        ChatWebSocketHandler.getOnlineSessions().forEach((userId, session) -> {
            try {
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (Exception e) {
                System.err.println("广播消息给用户 " + userId + " 失败: " + e.getMessage());
            }
        });
    }

    /**
     * 构造消息
     *
     * @param type 消息类型
     * @param message 消息内容
     * @return 消息JSON字符串
     */
    private String buildMessage(String type, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(response);
    }
}