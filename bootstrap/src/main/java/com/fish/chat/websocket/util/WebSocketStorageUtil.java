package com.fish.chat.websocket.util;

import com.fish.chat.dto.UserDTO;
import com.fish.chat.dto.WebSocketMessageDTO;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.MongoGroupMessage;
import com.fish.chat.mapper.redis.RedisOnlineUserMapper;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.GroupMessageService;
import com.fish.chat.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WebSocket存储工具类 用于处理Redis和MongoDB的存储操作
 */
@Component
public class WebSocketStorageUtil {

    private static RedisOnlineUserMapper redisOnlineUserMapper;
    private static ChatMessageService chatMessageService;
    private static GroupMessageService groupMessageService;
    private static UserService userService;

    /**
     * 保存用户在线信息到Redis
     *
     * @param userId 用户ID
     * @param userDTO 用户信息
     * @param expireMinutes 过期时间（分钟）
     */
    public static void saveOnlineUser(String userId, UserDTO userDTO, long expireMinutes) {
        redisOnlineUserMapper.saveOnlineUser(userId, userDTO, expireMinutes);
    }

    /**
     * 从Redis中删除用户在线信息
     *
     * @param userId 用户ID
     */
    public static void removeOnlineUser(String userId) {
        redisOnlineUserMapper.removeOnlineUser(userId);
    }

    /**
     * 更新Redis中用户在线状态的过期时间
     *
     * @param userId 用户ID
     * @param expireMinutes 过期时间（分钟）
     */
    public static void updateOnlineUserExpire(String userId, long expireMinutes) {
        redisOnlineUserMapper.updateOnlineUserExpire(userId, expireMinutes);
    }

    /**
     * 保存聊天消息到MongoDB
     *
     * @param messageDTO 消息DTO
     */
    public static void saveChatMessage(WebSocketMessageDTO messageDTO) {
        try {
            MongoChatMessage chatMessage = new MongoChatMessage();
            BeanUtils.copyProperties(messageDTO, chatMessage);
            chatMessageService.saveMessage(chatMessage);
        } catch (Exception e) {
            System.err.println("保存聊天记录到MongoDB失败: " + e.getMessage());
        }
    }

    /**
     * 保存群组消息到MongoDB
     *
     * @param messageDTO 消息DTO
     */
    public static void saveGroupMessage(WebSocketMessageDTO messageDTO) {
        try {
            MongoGroupMessage groupMessage = new MongoGroupMessage();
            BeanUtils.copyProperties(messageDTO, groupMessage);
            groupMessageService.saveGroupMessage(groupMessage);
        } catch (Exception e) {
            System.err.println("保存群组消息到MongoDB失败: " + e.getMessage());
        }
    }

    @Autowired
    public void setRedisOnlineUserMapper(RedisOnlineUserMapper redisOnlineUserMapper) {
        WebSocketStorageUtil.redisOnlineUserMapper = redisOnlineUserMapper;
    }

    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        WebSocketStorageUtil.chatMessageService = chatMessageService;
    }

    @Autowired
    public void setGroupMessageService(GroupMessageService groupMessageService) {
        WebSocketStorageUtil.groupMessageService = groupMessageService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        WebSocketStorageUtil.userService = userService;
    }
}