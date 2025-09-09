package com.fish.chat.utils.websocket;

import com.alibaba.fastjson.JSON;
import com.fish.chat.websocket.ChatWebSocket;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket工具类
 */
@Slf4j
public class WebSocketUtil {

    /**
     * 发送消息给指定用户
     * 
     * @param userId 用户ID
     * @param type 消息类型
     * @param message 消息内容
     */
    public static void sendMessageToUser(String userId, String type, String message) {
        ChatWebSocket client = ChatWebSocket.getOnlineClients().get(userId);
        if (client != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", type);
            response.put("message", message);
            response.put("timestamp", System.currentTimeMillis());
            
            client.sendMessageToSelf(JSON.toJSONString(response));
        } else {
            log.warn("用户 {} 不在线，无法发送消息", userId);
        }
    }

    /**
     * 发送消息给所有在线用户
     * 
     * @param type 消息类型
     * @param message 消息内容
     */
    public static void broadcastMessage(String type, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", type);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        
        String jsonMessage = JSON.toJSONString(response);
        
        ChatWebSocket.getOnlineClients().forEach((userId, client) -> {
            try {
                client.sendMessageToSelf(jsonMessage);
            } catch (Exception e) {
                log.error("广播消息给用户 " + userId + " 失败", e);
            }
        });
    }
}