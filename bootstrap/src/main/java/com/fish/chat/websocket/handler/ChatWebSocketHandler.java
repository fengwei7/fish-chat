package com.fish.chat.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.fish.chat.dto.UserDTO;
import com.fish.chat.dto.WebSocketMessageDTO;
import com.fish.chat.mapper.redis.RedisOnlineUserMapper;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.GroupService;
import com.fish.chat.service.UserService;
import com.fish.chat.websocket.util.WebSocketMessageUtil;
import com.fish.chat.websocket.util.WebSocketStorageUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket聊天处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 用于存放所有在线客户端
    private static final Map<String, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();
    
    // 群组成员缓存，key为groupId，value为该群组的成员userId集合
    private static final Map<String, Set<String>> groupMembersCache = new ConcurrentHashMap<>();
    
    // 线程池用于处理大量消息广播
    private static final ExecutorService broadcastExecutor = Executors.newFixedThreadPool(10);

    private static UserService userService;

    private static ChatMessageService chatMessageService;
    
    private static GroupService groupService;

    private static RedisOnlineUserMapper redisOnlineUserMapper;

    /**
     * 获取在线会话列表
     *
     * @return 在线会话列表
     */
    public static Map<String, WebSocketSession> getOnlineSessions() {
        return onlineSessions;
    }

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    public static int getOnlineCount() {
        return onlineSessions.size();
    }

    /**
     * 获取在线用户信息列表
     *
     * @return 在线用户信息列表
     */
    public static Map<String, UserDTO> getOnlineUsers() {
        return redisOnlineUserMapper.getAllOnlineUsers();
    }
    
    /**
     * 更新群组成员缓存
     * 
     * @param groupId 群组ID
     */
    public static void updateGroupMembersCache(String groupId) {
        // 实际项目中应该从数据库获取群组成员列表
        // 这里简化处理，仅作示例
        // Set<String> members = groupService.getGroupMembers(groupId).stream()
        //     .map(m -> String.valueOf(m.getUserId()))
        //     .collect(Collectors.toSet());
        // groupMembersCache.put(groupId, members);
    }

    @Autowired
    public void setUserService(UserService userService) {
        ChatWebSocketHandler.userService = userService;
    }

    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        ChatWebSocketHandler.chatMessageService = chatMessageService;
    }
    
    @Autowired
    public void setGroupService(GroupService groupService) {
        ChatWebSocketHandler.groupService = groupService;
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
            String payload = message.getPayload();
            Map<String, Object> msg = JSON.parseObject(payload, Map.class);
            String type = (String) msg.get("type");

            switch (type) {
                case "chat":
                    // 处理私聊消息
                    handleChatMessage(userIdStr, msg);
                    break;
                case "group":
                    // 处理群组消息
                    handleGroupMessage(userIdStr, msg);
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
     * 处理私聊消息
     *
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
     * 处理群组消息
     *
     * @param fromUserId 发送用户ID
     * @param msg 消息内容
     */
    private void handleGroupMessage(String fromUserId, Map<String, Object> msg) throws Exception {
        String groupId = (String) msg.get("groupId");
        String content = (String) msg.get("content");
        
        // 检查用户是否在群组中
        if (!groupService.isGroupMember(Long.valueOf(groupId), Long.valueOf(fromUserId))) {
            WebSocketSession fromSession = onlineSessions.get(fromUserId);
            if (fromSession != null && fromSession.isOpen()) {
                sendMessage(fromSession, WebSocketMessageUtil.buildErrorMessage("您不在该群组中"));
            }
            return;
        }
        
        // 构造群组消息
        String groupMessage = WebSocketMessageUtil.buildGroupMessage(fromUserId, groupId, content, System.currentTimeMillis());
        
        // 异步广播消息给群组成员
        broadcastExecutor.submit(() -> {
            try {
                broadcastGroupMessage(groupId, groupMessage, fromUserId);
            } catch (Exception e) {
                System.err.println("广播群组消息失败: " + e.getMessage());
            }
        });
        
        // 持久化群组消息到MongoDB
        WebSocketMessageDTO messageDTO = new WebSocketMessageDTO();
        messageDTO.setType("group");
        messageDTO.setFrom(fromUserId);
        messageDTO.setTo(groupId); // 这里用groupId代替to字段
        messageDTO.setContent(content);
        messageDTO.setTimestamp(System.currentTimeMillis());
        WebSocketStorageUtil.saveGroupMessage(messageDTO);
    }
    
    /**
     * 广播群组消息给所有在线成员
     * 
     * @param groupId 群组ID
     * @param message 消息内容
     * @param fromUserId 发送者ID（用于排除自己）
     */
    private void broadcastGroupMessage(String groupId, String message, String fromUserId) {
        // 获取群组成员（实际项目中应该使用缓存优化）
        List<com.fish.chat.entity.GroupMember> groupMembers = groupService.getGroupMembers(Long.valueOf(groupId));
        List<String> memberUserIds = groupMembers.stream()
                .map(member -> String.valueOf(member.getUserId()))
                .collect(Collectors.toList());
        
        // 过滤出在线的群组成员
        List<String> onlineMembers = memberUserIds.stream()
                .filter(userId -> onlineSessions.containsKey(userId))
                .collect(Collectors.toList());
        
        System.out.println("群组 " + groupId + " 在线成员数: " + onlineMembers.size());
        
        // 发送给每个在线成员
        for (String userId : onlineMembers) {
            // 不发送给自己
            if (userId.equals(fromUserId)) {
                continue;
            }
            
            try {
                WebSocketSession session = onlineSessions.get(userId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                }
            } catch (Exception e) {
                System.err.println("发送群组消息给用户 " + userId + " 失败: " + e.getMessage());
            }
        }
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
     *
     * @param session WebSocket会话
     * @param message 消息内容
     */
    private void sendMessage(WebSocketSession session, String message) throws Exception {
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }
}