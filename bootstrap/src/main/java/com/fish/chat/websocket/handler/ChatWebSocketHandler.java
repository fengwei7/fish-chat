package com.fish.chat.websocket.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.fish.chat.dto.UserDTO;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.User;
import com.fish.chat.mapper.redis.RedisOnlineUserMapper;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.UserService;
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
    
    // 用于存放所有在线用户信息
    private static final Map<String, UserDTO> onlineUsers = new ConcurrentHashMap<>();

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

        // 获取用户信息
        User user = userService.getById(userId);

        // 构造UserDTO
        UserDTO userDTO = null;
        if (user != null) {
            userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            // 将用户信息保存到Redis
            redisOnlineUserMapper.saveOnlineUser(userIdStr, userDTO, 5);
        }

        // 将客户端连接加入在线列表
        onlineSessions.put(userIdStr, session);
        if (userDTO != null) {
            onlineUsers.put(userIdStr, userDTO);
        }

        System.out.println("用户 " + userId + " 连接成功，当前在线人数: " + onlineSessions.size());

        // 发送连接成功消息
        Map<String, Object> connectMsg = new HashMap<>();
        connectMsg.put("type", "connect");
        connectMsg.put("message", "连接成功");
        sendMessage(session, JSON.toJSONString(connectMsg));
    }

    // 监听：连接关闭
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = Long.valueOf((String) session.getAttributes().get("userId"));

        String userIdStr = String.valueOf(userId);

        // 从在线列表中移除
        onlineSessions.remove(userIdStr);
        onlineUsers.remove(userIdStr);

        // 从Redis中删除用户在线状态
        redisOnlineUserMapper.removeOnlineUser(userIdStr);

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
                    Map<String, Object> errorMsg = new HashMap<>();
                    errorMsg.put("type", "error");
                    errorMsg.put("message", "未知消息类型");
                    sendMessage(session, JSON.toJSONString(errorMsg));
                    break;
            }
        } catch (Exception e) {
            System.err.println("处理消息失败: " + e.getMessage());
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("type", "error");
            errorMsg.put("message", "消息处理失败");
            sendMessage(session, JSON.toJSONString(errorMsg));
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
        Map<String, Object> response = new HashMap<>();
        response.put("type", "chat");
        response.put("from", fromUserId);
        response.put("content", content);
        response.put("timestamp", System.currentTimeMillis());
        
        // 发送给指定用户
        WebSocketSession toSession = onlineSessions.get(toUserId);
        if (toSession != null && toSession.isOpen()) {
            sendMessage(toSession, JSON.toJSONString(response));
        } else {
            // 用户不在线，返回错误信息给发送方
            WebSocketSession fromSession = onlineSessions.get(fromUserId);
            if (fromSession != null && fromSession.isOpen()) {
                Map<String, Object> offlineMsg = new HashMap<>();
                offlineMsg.put("type", "error");
                offlineMsg.put("message", "用户不在线");
                sendMessage(fromSession, JSON.toJSONString(offlineMsg));
            }
        }
        
        // 持久化聊天记录到MongoDB
        try {
            MongoChatMessage chatMessage = new MongoChatMessage();
            chatMessage.setType("chat");
            chatMessage.setFrom(fromUserId);
            chatMessage.setTo(toUserId);
            chatMessage.setContent(content);
            chatMessage.setTimestamp(System.currentTimeMillis());
            chatMessageService.saveMessage(chatMessage);
        } catch (Exception e) {
            System.err.println("保存聊天记录到MongoDB失败: " + e.getMessage());
        }
    }

    /**
     * 处理心跳消息
     */
    private void handlePingMessage(String userId) {
        // 更新Redis中用户在线状态的过期时间
        redisOnlineUserMapper.updateOnlineUserExpire(userId, 5);
        
        WebSocketSession session = onlineSessions.get(userId);
        if (session != null && session.isOpen()) {
            Map<String, Object> pongMsg = new HashMap<>();
            pongMsg.put("type", "pong");
            try {
                sendMessage(session, JSON.toJSONString(pongMsg));
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
        return onlineUsers;
    }
}