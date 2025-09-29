package com.fish.chat.websocket.controller;

import com.fish.chat.dto.WebSocketMessageDTO;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.GroupService;
import com.fish.chat.service.UserService;
import com.fish.chat.websocket.service.GroupCacheService;
import com.fish.chat.websocket.util.WebSocketMessageUtil;
import com.fish.chat.websocket.util.WebSocketStorageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class StompChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private GroupCacheService groupCacheService;
    
    @Autowired
    private SimpUserRegistry userRegistry;
    
    /**
     * 处理私聊消息
     * 客户端发送到 /app/chat
     */
    @MessageMapping("/chat")
    public void handleChatMessage(@Payload Map<String, Object> msg) {
        String fromUserId = msg.get("from").toString();
        String toUserId = msg.get("to").toString();
        String content = (String) msg.get("content");
        
        System.out.println("来自用户 " + fromUserId + " 的私聊消息: " + content);
        
        // 构造返回消息
        String response = WebSocketMessageUtil.buildChatMessage(fromUserId, content, System.currentTimeMillis());
        
        // 检查目标用户是否在线
        boolean isUserOnline = userRegistry.getUsers().stream()
                .anyMatch(user -> user.getName().equals(toUserId));
        
        if (isUserOnline) {
            // 发送给指定用户
            messagingTemplate.convertAndSendToUser(toUserId, "/queue/messages", response);
        } else {
            // 用户不在线，返回错误信息给发送方
            String errorMessage = WebSocketMessageUtil.buildUserOfflineMessage();
            messagingTemplate.convertAndSendToUser(fromUserId, "/queue/errors", errorMessage);
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
     * 客户端发送到 /app/group
     */
    @MessageMapping("/group")
    public void handleGroupMessage(@Payload Map<String, Object> msg) {
        String fromUserId = msg.get("from").toString();
        String groupId = msg.get("groupId").toString();
        String content = (String) msg.get("content");
        
        System.out.println("来自用户 " + fromUserId + " 的群组消息: " + content);
        
        // 检查用户是否在群组中
        if (!groupService.isGroupMember(Long.valueOf(groupId), Long.valueOf(fromUserId))) {
            String errorMessage = WebSocketMessageUtil.buildErrorMessage("您不在该群组中");
            messagingTemplate.convertAndSendToUser(fromUserId, "/queue/errors", errorMessage);
            return;
        }
        
        // 构造群组消息
        String groupMessage = WebSocketMessageUtil.buildGroupMessage(fromUserId, groupId, content, System.currentTimeMillis());
        
        // 优化：使用topic广播消息给所有订阅该群组的客户端
        // 客户端需要订阅 /topic/group/{groupId} 主题来接收消息
        messagingTemplate.convertAndSend("/topic/group/" + groupId, groupMessage);
        
        // 持久化群组消息到MongoDB
        WebSocketMessageDTO messageDTO = new WebSocketMessageDTO();
        messageDTO.setType("group");
        messageDTO.setFrom(fromUserId);
        messageDTO.setTo(groupId);
        messageDTO.setContent(content);
        messageDTO.setTimestamp(System.currentTimeMillis());
        WebSocketStorageUtil.saveGroupMessage(messageDTO);
    }
    
    /**
     * 处理心跳消息
     * 客户端发送到 /app/ping
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public String handlePingMessage(@Payload Map<String, Object> msg) {
        String userId = msg.get("from").toString();
        System.out.println("收到用户 " + userId + " 的心跳消息");
        return WebSocketMessageUtil.buildPongMessage();
    }
    
    /**
     * 更新群组成员缓存
     * 
     * @param groupId 群组ID
     */
    public void updateGroupMembersCache(String groupId) {
        groupCacheService.updateGroupMembersCache(groupId);
    }
}