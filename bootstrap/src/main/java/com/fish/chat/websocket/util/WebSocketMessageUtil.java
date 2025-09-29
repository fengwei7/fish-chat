package com.fish.chat.websocket.util;

import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息工具类
 */
public class WebSocketMessageUtil {

    /**
     * 构造连接成功消息
     *
     * @param message 消息内容
     * @return 连接成功消息JSON字符串
     */
    public static String buildConnectMessage(String message) {
        Map<String, Object> connectMsg = new HashMap<>();
        connectMsg.put("type", "connect");
        connectMsg.put("message", message);
        return JSON.toJSONString(connectMsg);
    }

    /**
     * 构造聊天消息
     *
     * @param fromUserId 发送方用户ID
     * @param content 消息内容
     * @param timestamp 时间戳
     * @return 聊天消息JSON字符串
     */
    public static String buildChatMessage(String fromUserId, String content, long timestamp) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "chat");
        response.put("from", fromUserId);
        response.put("content", content);
        response.put("timestamp", timestamp);
        return JSON.toJSONString(response);
    }

    /**
     * 构造群组消息
     *
     * @param fromUserId 发送方用户ID
     * @param groupId 群组ID
     * @param content 消息内容
     * @param timestamp 时间戳
     * @return 群组消息JSON字符串
     */
    public static String buildGroupMessage(String fromUserId, String groupId, String content, long timestamp) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "group");
        response.put("from", fromUserId);
        response.put("groupId", groupId);
        response.put("content", content);
        response.put("timestamp", timestamp);
        return JSON.toJSONString(response);
    }

    /**
     * 构造错误消息
     *
     * @param message 错误消息内容
     * @return 错误消息JSON字符串
     */
    public static String buildErrorMessage(String message) {
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("type", "error");
        errorMsg.put("message", message);
        return JSON.toJSONString(errorMsg);
    }

    /**
     * 构造心跳响应消息
     *
     * @return 心跳响应消息JSON字符串
     */
    public static String buildPongMessage() {
        Map<String, Object> pongMsg = new HashMap<>();
        pongMsg.put("type", "pong");
        return JSON.toJSONString(pongMsg);
    }

    /**
     * 构造用户离线消息
     *
     * @return 用户离线消息JSON字符串
     */
    public static String buildUserOfflineMessage() {
        Map<String, Object> offlineMsg = new HashMap<>();
        offlineMsg.put("type", "error");
        offlineMsg.put("message", "用户不在线");
        return JSON.toJSONString(offlineMsg);
    }
}