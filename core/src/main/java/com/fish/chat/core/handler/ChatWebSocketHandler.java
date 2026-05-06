package com.fish.chat.core.handler;

import com.alibaba.fastjson.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.repository.ChatMessageRepository;
import com.fish.chat.core.repository.UserOnlineRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 聊天处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    // 在线会话管理
    private static final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    @Resource
    private UserOnlineRepository userOnlineRepository;

    @Resource
    private ChatMessageRepository chatMessageRepository;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        
        // 保存用户到 Redis
        UserDTO userDTO = new UserDTO();
        userOnlineRepository.saveOnlineUser(userId, userDTO, 5);
        
        // 加入在线列表
        onlineSessions.put(userId, session);
        
        System.out.println("用户 " + userId + " 连接成功，当前在线人数：" + onlineSessions.size());
        
        // 发送连接成功消息
        sendMessage(session, buildMessage("connect", "连接成功"));
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        
        // 移除在线状态
        onlineSessions.remove(userId);
        userOnlineRepository.removeOnlineUser(userId);
        
        System.out.println("用户 " + userId + " 断开连接，当前在线人数：" + onlineSessions.size());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        System.out.println("收到用户 " + userId + " 的消息：" + message.getPayload());
        
        try {
            // 解析消息
            Map<String, Object> msg = JSON.parseObject(message.getPayload(), Map.class);
            String type = (String) msg.get("type");
            
            switch (type) {
                case "chat":
                    handleChatMessage(userId, msg);
                    break;
                case "ping":
                    handlePingMessage(userId);
                    break;
                default:
                    sendMessage(session, buildMessage("error", "未知消息类型"));
            }
        } catch (Exception e) {
            System.err.println("处理消息失败：" + e.getMessage());
            sendMessage(session, buildMessage("error", "消息处理失败"));
        }
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(String fromUserId, Map<String, Object> msg) throws Exception {
        String toUserId = (String) msg.get("to");
        String content = (String) msg.get("content");
        
        // 发送给目标用户
        WebSocketSession toSession = onlineSessions.get(toUserId);
        if (toSession != null && toSession.isOpen()) {
            String response = buildMessage("chat", content);
            toSession.sendMessage(new TextMessage(response));
        } else {
            // 用户不在线，返回错误信息给发送者
            WebSocketSession fromSession = onlineSessions.get(fromUserId);
            if (fromSession != null && fromSession.isOpen()) {
                sendMessage(fromSession, buildMessage("error", "用户不在线"));
            }
        }
        
        // 保存到 MongoDB
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType("chat");
        chatMessage.setFrom(fromUserId);
        chatMessage.setTo(toUserId);
        chatMessage.setContent(content);
        chatMessage.setTimestamp(System.currentTimeMillis());
        chatMessageRepository.save(chatMessage);
    }
    
    /**
     * 处理心跳消息
     */
    private void handlePingMessage(String userId) {
        // 更新 Redis 过期时间
        userOnlineRepository.updateOnlineUserExpire(userId, AuthConstants.ONLINE_USER_EXPIRE_MINUTES);
        
        WebSocketSession session = onlineSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, buildMessage("pong", ""));
            } catch (Exception e) {
                System.err.println("发送心跳响应失败：" + e.getMessage());
            }
        }
    }
    
    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, String message) throws Exception {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
    
    /**
     * 构建消息 JSON
     */
    private String buildMessage(String type, String content) {
        Map<String, Object> msg = new ConcurrentHashMap<>();
        msg.put("type", type);
        msg.put("content", content);
        msg.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(msg);
    }
}
