package com.fish.chat.websocket.controller;

import com.fish.chat.dto.WebSocketMessageDTO;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.GroupMessageService;
import com.fish.chat.service.GroupService;
import com.fish.chat.service.UserService;
import com.fish.chat.websocket.service.GroupCacheService;
import com.fish.chat.websocket.util.WebSocketMessageUtil;
import com.fish.chat.websocket.util.WebSocketStorageUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class StompChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private GroupMessageService groupMessageService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupCacheService groupCacheService;

    @Autowired
    private SimpUserRegistry userRegistry;

    /**
     * 处理私聊消息 客户端发送到 /app/chat
     */
    @MessageMapping("/chat")
    public void handleChatMessage(@Payload Map<String, Object> msg) {
        log.info("收到私聊消息: {}", msg);
        
        String fromUserId = msg.get("from").toString();
        String toUserId = msg.get("to").toString();
        String content = (String) msg.get("content");

        log.info("来自用户 {} 的私聊消息: {}", fromUserId, content);

        // 构造返回消息
        String response = WebSocketMessageUtil.buildChatMessage(fromUserId, content, System.currentTimeMillis());

        // 检查目标用户是否在线
        boolean isUserOnline = userRegistry.getUsers().stream()
            .anyMatch(user -> user.getName().equals(toUserId));

        if (isUserOnline) {
            // 发送给指定用户
            messagingTemplate.convertAndSendToUser(toUserId, "/queue/messages", response);
            log.info("私聊消息已发送给用户 {}", toUserId);
        } else {
            // 用户不在线，返回错误信息给发送方
            String errorMessage = WebSocketMessageUtil.buildUserOfflineMessage();
            messagingTemplate.convertAndSendToUser(fromUserId, "/queue/errors", errorMessage);
            log.info("目标用户 {} 不在线", toUserId);
        }

        // 持久化聊天记录到MongoDB
        WebSocketMessageDTO messageDTO = new WebSocketMessageDTO();
        messageDTO.setType("chat");
        messageDTO.setFrom(fromUserId);
        messageDTO.setTo(toUserId);
        messageDTO.setContent(content);
        messageDTO.setTimestamp(System.currentTimeMillis());
        WebSocketStorageUtil.saveChatMessage(messageDTO);
        log.info("私聊消息已持久化到数据库");
    }

    /**
     * 处理群组消息 客户端发送到 /app/group
     */
    @MessageMapping("/group")
    public void handleGroupMessage(@Payload Map<String, Object> msg) {
        log.info("收到群组消息: {}", msg);
        
        String fromUserId = msg.get("from").toString();
        String groupId = msg.get("groupId").toString();
        String content = (String) msg.get("content");

        log.info("来自用户 {} 的群组消息: {}", fromUserId, content);

        // 检查用户是否在群组中
        if (!groupService.isGroupMember(Long.valueOf(groupId), Long.valueOf(fromUserId))) {
            String errorMessage = WebSocketMessageUtil.buildErrorMessage("您不在该群组中");
            messagingTemplate.convertAndSendToUser(fromUserId, "/queue/errors", errorMessage);
            log.info("用户 {} 不在群组 {} 中，已通知", fromUserId, groupId);
            return;
        }

        // 构造群组消息
        String groupMessage = WebSocketMessageUtil.buildGroupMessage(fromUserId, groupId, content,
            System.currentTimeMillis());

        // 优化：使用topic广播消息给所有订阅该群组的客户端
        // 客户端需要订阅 /topic/group/{groupId} 主题来接收消息
        messagingTemplate.convertAndSend("/topic/group/" + groupId, groupMessage);
        log.info("群组消息已广播到群组 {}", groupId);

        // 持久化群组消息到MongoDB
        WebSocketMessageDTO messageDTO = new WebSocketMessageDTO();
        messageDTO.setType("group");
        messageDTO.setFrom(fromUserId);
        messageDTO.setTo(groupId);
        messageDTO.setContent(content);
        messageDTO.setTimestamp(System.currentTimeMillis());
        WebSocketStorageUtil.saveGroupMessage(messageDTO);
        log.info("群组消息已持久化到数据库");
    }

    /**
     * 处理心跳消息 客户端发送到 /app/ping
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public String handlePingMessage(@Payload Map<String, Object> msg) {
        String userId = msg.get("from").toString();
        log.info("收到用户 {} 的心跳消息", userId);
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