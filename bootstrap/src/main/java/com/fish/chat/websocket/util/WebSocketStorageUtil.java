package com.fish.chat.websocket.util;

import com.fish.chat.dto.UserDTO;
import com.fish.chat.dto.WebSocketMessageDTO;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.User;
import com.fish.chat.mapper.redis.RedisOnlineUserMapper;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WebSocket存储工具类
 * 用于处理Redis和MongoDB的存储操作
 */
@Component
public class WebSocketStorageUtil {

    private static RedisOnlineUserMapper redisOnlineUserMapper;
    private static ChatMessageService chatMessageService;
    private static UserService userService;

    @Autowired
    public void setRedisOnlineUserMapper(RedisOnlineUserMapper redisOnlineUserMapper) {
        WebSocketStorageUtil.redisOnlineUserMapper = redisOnlineUserMapper;
    }

    @Autowired
    public void setChatMessageService(ChatMessageService chatMessageService) {
        WebSocketStorageUtil.chatMessageService = chatMessageService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        WebSocketStorageUtil.userService = userService;
    }

    /**
     * 保存用户在线信息到Redis
     *
     * @param userId 用户ID
     * @param expireMinutes 过期时间（分钟）
     */
    public static void saveOnlineUser(String userId, long expireMinutes) {
        // 获取用户信息
        User user = userService.getById(Long.valueOf(userId));
        
        // 构造UserDTO
        UserDTO userDTO = null;
        if (user != null) {
            userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            
            redisOnlineUserMapper.saveOnlineUser(userId, userDTO, expireMinutes);
        }
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
}