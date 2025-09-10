package com.fish.chat.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.fish.chat.dto.UserDTO;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.User;
import com.fish.chat.mapper.redis.RedisOnlineUserMapper;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天服务端点
 */
@Slf4j
@Component
@ServerEndpoint("/websocket/chat")
public class ChatWebSocket {

    // 用于存放所有在线客户端
    private static final Map<String, ChatWebSocket> onlineClients = new ConcurrentHashMap<>();
    
    // 与某个客户端的连接会话
    private Session session;
    
    // 用户ID
    private String userId;

    
    // 用户信息
    private UserDTO userDTO;
    
    // 注入UserService (通过静态方法注入)
    private static UserService userService;
    
    // 注入ChatMessageService (通过静态方法注入)
    private static ChatMessageService chatMessageService;
    
    // 注入RedisOnlineUserMapper (通过静态方法注入)
    private static RedisOnlineUserMapper redisOnlineUserMapper;



    @Autowired
    public void setUserService(UserService userService) {
        ChatWebSocket.userService = userService;
    }
    
    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        ChatWebSocket.chatMessageService = chatMessageService;
    }
    
    @Autowired
    public void setRedisOnlineUserMapper(com.fish.chat.mapper.redis.RedisOnlineUserMapper redisOnlineUserMapper) {
        ChatWebSocket.redisOnlineUserMapper = redisOnlineUserMapper;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        // 检查用户是否已登录
        if (!StpUtil.isLogin()) {
            // 未登录，关闭连接
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "用户未登录"));
            } catch (Exception e) {
                log.error("关闭未认证连接失败", e);
            }
            return;
        }
        
        this.session = session;
        this.userId = StpUtil.getLoginIdAsString();

        User user = userService.getById(userId);
        
        // 构造UserDTO
        if (user != null) {
            this.userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            // 将用户信息保存到Redis
            redisOnlineUserMapper.saveOnlineUser(userId, userDTO, 5);
        }
        
        // 将客户端连接加入在线列表
        onlineClients.put(userId, this);
        log.info("用户 {} 连接成功，当前在线人数: {}", userId, onlineClients.size());
        
        // 发送连接成功消息
        Map<String, Object> connectMsg = new HashMap<>();
        connectMsg.put("type", "connect");
        connectMsg.put("message", "连接成功");
        sendMessageToSelf(JSON.toJSONString(connectMsg));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        // 从在线列表中移除
        onlineClients.remove(userId);
        // 从Redis中删除用户在线状态
        if (redisOnlineUserMapper != null) {
            redisOnlineUserMapper.removeOnlineUser(userId);
        }
        log.info("用户 {} 断开连接，当前在线人数: {}", userId, onlineClients.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自用户 {} 的消息: {}", userId, message);
        
        try {
            // 解析消息
            Map<String, Object> msg = JSON.parseObject(message, Map.class);
            String type = (String) msg.get("type");
            
            switch (type) {
                case "chat":
                    // 处理聊天消息
                    handleChatMessage(msg);
                    break;
                case "ping":
                    // 处理心跳消息
                    handlePingMessage();
                    break;
                default:
                    Map<String, Object> errorMsg = new HashMap<>();
                    errorMsg.put("type", "error");
                    errorMsg.put("message", "未知消息类型");
                    sendMessageToSelf(JSON.toJSONString(errorMsg));
                    break;
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
            Map<String, Object> errorMsg = new HashMap<>();
            errorMsg.put("type", "error");
            errorMsg.put("message", "消息处理失败");
            sendMessageToSelf(JSON.toJSONString(errorMsg));
        }
    }

    /**
     * 处理聊天消息
     * @param msg 消息内容
     */
    private void handleChatMessage(Map<String, Object> msg) {
        String toUserId = (String) msg.get("to");
        String content = (String) msg.get("content");
        
        // 构造返回消息
        Map<String, Object> response = new HashMap<>();
        response.put("type", "chat");
        response.put("from", userId);
        response.put("content", content);
        response.put("timestamp", System.currentTimeMillis());
        
        // 发送给指定用户
        ChatWebSocket toClient = onlineClients.get(toUserId);
        if (toClient != null) {
            toClient.sendMessageToSelf(JSON.toJSONString(response));
        } else {
            // 用户不在线，返回错误信息
            Map<String, Object> offlineMsg = new HashMap<>();
            offlineMsg.put("type", "error");
            offlineMsg.put("message", "用户不在线");
            sendMessageToSelf(JSON.toJSONString(offlineMsg));
        }
        
        // 持久化聊天记录到MongoDB
        try {
            MongoChatMessage chatMessage = new MongoChatMessage();
            chatMessage.setType("chat");
            chatMessage.setFrom(userId);
            chatMessage.setTo(toUserId);
            chatMessage.setContent(content);
            chatMessage.setTimestamp(System.currentTimeMillis());
            chatMessageService.saveMessage(chatMessage);
        } catch (Exception e) {
            log.error("保存聊天记录到MongoDB失败", e);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handlePingMessage() {
        // 更新Redis中用户在线状态的过期时间
        if (redisOnlineUserMapper != null) {
            redisOnlineUserMapper.updateOnlineUserExpire(userId, 5);
        }
        
        Map<String, Object> pongMsg = new HashMap<>();
        pongMsg.put("type", "pong");
        sendMessageToSelf(JSON.toJSONString(pongMsg));
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误", error);
        onlineClients.remove(userId);
        // 从Redis中删除用户在线状态
        if (redisOnlineUserMapper != null) {
            redisOnlineUserMapper.removeOnlineUser(userId);
        }
    }

    /**
     * 发送消息给自己
     * @param message 消息内容
     */
    public void sendMessageToSelf(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 获取在线客户端列表
     * @return 在线客户端列表
     */
    public static Map<String, ChatWebSocket> getOnlineClients() {
        return onlineClients;
    }

    public static Object getOnlineCount() {
        return onlineClients.size();
    }

    /**
     * 获取在线用户信息列表（从Redis获取）
     * @return 在线用户信息列表
     */
    public static Map<String, UserDTO> getOnlineUsers() {
        if (redisOnlineUserMapper != null) {
            return redisOnlineUserMapper.getAllOnlineUsers();
        }
        return new HashMap<>();
    }
}