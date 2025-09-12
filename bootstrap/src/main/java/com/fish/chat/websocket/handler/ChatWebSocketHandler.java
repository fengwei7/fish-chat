package com.fish.chat.websocket.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.fish.chat.dto.UserDTO;
import com.fish.chat.dto.WebSocketMessageDTO;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.User;
import com.fish.chat.mapper.redis.RedisOnlineUserMapper;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.UserService;
import com.fish.chat.websocket.util.WebSocketMessageUtil;
import com.fish.chat.websocket.util.WebSocketStorageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 用于存放所有在线客户端
    private static final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();
    
    private static UserService userService;

    private static ChatMessageService chatMessageService;

    private static RedisOnlineUserMapper redisOnlineUserMapper;

    @Autowired
    public void setUserService(UserService userService) {
        ChatWebSocketHandler.userService = userService;
    }
    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        ChatWebSocketHandler.chatMessageService = chatMessageService;
    }
    @Autowired
    public void setRedisOnlineUserMapper(RedisOnlineUserMapper redisOnlineUserMapper) {
        ChatWebSocketHandler.redisOnlineUserMapper = redisOnlineUserMapper;
    }

    // 监听：连接开启
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话属性中获取用户ID（在拦截器中设置）
        Long userId = Long.valueOf((String) session.getAttributes().get("userId"));

        String userIdStr = String.valueOf(userId);

        // 将用户信息保存到Redis
        WebSocketStorageUtil.saveOnlineUser(userIdStr, 5);

        // 将客户端连接加入在线列表
        onlineSessions.put(userIdStr, session);

        System.out.println("用户 " + userId + " 连接成功，当前在线人数: " + onlineSessions.size());

        // 发送连接成功消息
        sendMessage(session, WebSocketMessageUtil.buildConnectMessage("连接成功"));
    }

    // 监听：连接关闭
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = Long.valueOf((String) session.getAttributes().get("userId"));

        String userIdStr = String.valueOf(userId);

        // 从在线列表中移除
        onlineSessions.remove(userIdStr);

        // 从Redis中删除用户在线状态
        WebSocketStorageUtil.removeOnlineUser(userIdStr);

        System.out.println("用户 " + userId + " 断开连接，当前在线人数: " + onlineSessions.size());
    }

    // 收到消息
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = Long.valueOf((String) session.getAttributes().get("userId"));

        String userIdStr = String.valueOf(userId);
        System.out.println("来自用户 " + userId + " 的消息: " + message.getPayload());

        try {
            // 解析消息
            Map<String, Object> msg = JSON.parseObject(message.getPayload(), Map.class);
            String type = (String) msg.get("type");

            switch (type) {
                case "chat":
                    // 处理聊天消息
                    handleChatMessage(userIdStr, msg);
                    break;
                case "ping":
                    // 处理心跳消息
                    handlePingMessage(userIdStr);
                    break;
                default:
                    sendMessage(session, WebSocketMessageUtil.buildErrorMessage("未知消息类型"));
                    break;
            }
        } catch (Exception e) {
            System.err.println("处理消息失败: " + e.getMessage());
            sendMessage(session, WebSocketMessageUtil.buildErrorMessage("消息处理失败"));
        }
    }

    /**
     * 处理聊天消息
     * @param fromUserId 发送用户ID
     * @param msg 消息内容
     */
    private void handleChatMessage(String fromUserId, Map<String, Object> msg) throws Exception {
        String toUserId = (String) msg.get("to");
        String content = (String) msg.get("content");
        
        // 构造返回消息
        String response = WebSocketMessageUtil.buildChatMessage(fromUserId, content, System.currentTimeMillis());
        
        // 发送给指定用户
        WebSocketSession toSession = onlineSessions.get(toUserId);
        if (toSession != null && toSession.isOpen()) {
            sendMessage(toSession, response);
        } else {
            // 用户不在线，返回错误信息给发送方
            WebSocketSession fromSession = onlineSessions.get(fromUserId);
            if (fromSession != null && fromSession.isOpen()) {
                sendMessage(fromSession, WebSocketMessageUtil.buildUserOfflineMessage());
            }
        }
        
        // 持久化聊天记录到MongoDB
        WebSocketMessageDTO messageDTO = new WebSocketMessageDTO();
        messageDTO.setType("chat");
        messageDTO.setFrom(fromUserId);
        messageDTO.setTo(toUserId);
        messageDTO.setContent(content);
        messageDTO.setTimestamp(System.currentTimeMillis());
        WebSocketStorageUtil.saveChatMessage(messageDTO);
    }

    /**
     * 处理心跳消息
     */
    private void handlePingMessage(String userId) {
        // 更新Redis中用户在线状态的过期时间
        WebSocketStorageUtil.updateOnlineUserExpire(userId, 5);
        
        WebSocketSession session = onlineSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, WebSocketMessageUtil.buildPongMessage());
            } catch (Exception e) {
                System.err.println("发送心跳响应失败: " + e.getMessage());
            }
        }
    }

    /**
     * 发送消息给指定会话
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void sendMessage(WebSocketSession session, String message) throws Exception {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    /**
     * 获取在线会话列表
     * @return 在线会话列表
     */
    public static Map<String, WebSocketSession> getOnlineSessions() {
        return onlineSessions;
    }

    /**
     * 获取在线用户数
     * @return 在线用户数
     */
    public static int getOnlineCount() {
        return onlineSessions.size();
    }

    /**
     * 获取在线用户信息列表
     * @return 在线用户信息列表
     */
    public static Map<String, UserDTO> getOnlineUsers() {
        return redisOnlineUserMapper.getAllOnlineUsers();
    }
}