package com.fish.chat.websocket.util;

import com.alibaba.fastjson.JSON;
import com.fish.chat.websocket.handler.ChatWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket工具类
 */
public class WebSocketUtil {

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param type    消息类型
     * @param message 消息内容
     */
    public static void sendMessageToUser(String userId, String type, String message) {
        WebSocketSession session = ChatWebSocketHandler.getOnlineSessions().get(userId);
        if (session != null && session.isOpen()) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", type);
            response.put("message", message);
            response.put("timestamp", System.currentTimeMillis());

            try {
                session.sendMessage(
                    new org.springframework.web.socket.TextMessage(
                        JSON.toJSONString(response)
                    )
                );
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
     * @param type    消息类型
     * @param message 消息内容
     */
    public static void broadcastMessage(String type, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        String jsonMessage = JSON.toJSONString(response);

        ChatWebSocketHandler.getOnlineSessions().forEach((userId, session) -> {
            try {
                if (session != null && session.isOpen()) {
                    session.sendMessage(
                        new org.springframework.web.socket.TextMessage(jsonMessage)
                    );
                }
            } catch (Exception e) {
                System.err.println("广播消息给用户 " + userId + " 失败: " + e.getMessage());
            }
        });
    }
}