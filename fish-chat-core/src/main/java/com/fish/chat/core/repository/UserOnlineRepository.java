package com.fish.chat.core.repository;

import com.alibaba.fastjson.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.core.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis 在线用户仓储
 */
@Repository
public class UserOnlineRepository {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 保存在线用户到 Redis
     * @param userId 用户 ID
     * @param userDTO 用户信息
     * @param expireMinutes 过期时间（分钟）
     */
    public void saveOnlineUser(String userId, UserDTO userDTO, long expireMinutes) {
        String key = AuthConstants.ONLINE_USER_PREFIX + userId;
        String value = JSON.toJSONString(userDTO);
        redisTemplate.opsForValue().set(key, value, expireMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * 更新在线用户过期时间
     * @param userId 用户 ID
     * @param expireMinutes 过期时间（分钟）
     */
    public void updateOnlineUserExpire(String userId, long expireMinutes) {
        String key = AuthConstants.ONLINE_USER_PREFIX + userId;
        redisTemplate.expire(key, expireMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * 删除在线用户
     * @param userId 用户 ID
     */
    public void removeOnlineUser(String userId) {
        String key = AuthConstants.ONLINE_USER_PREFIX + userId;
        redisTemplate.delete(key);
    }
    
    /**
     * 获取在线用户信息
     * @param userId 用户 ID
     * @return 用户信息，不存在返回 null
     */
    public UserDTO getOnlineUser(String userId) {
        String key = AuthConstants.ONLINE_USER_PREFIX + userId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return JSON.parseObject(value, UserDTO.class);
    }
    
    /**
     * 检查用户是否在线
     * @param userId 用户 ID
     * @return true-在线 false-离线
     */
    public boolean isOnline(String userId) {
        String key = AuthConstants.ONLINE_USER_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 获取所有在线用户 ID 列表
     * 使用 SCAN 命令替代 KEYS，避免阻塞 Redis
     * @return 在线用户 ID 集合
     */
    public Set<String> getAllOnlineUserIds() {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match((AuthConstants.ONLINE_USER_PREFIX + "*").getBytes())
                    .count(100)
                    .build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            }
            return null;
        });
        
        // 移除前缀，只保留用户 ID
        Set<String> userIds = new HashSet<>();
        for (String key : keys) {
            String userId = key.replace(AuthConstants.ONLINE_USER_PREFIX, "");
            userIds.add(userId);
        }
        return userIds;
    }
    
    /**
     * 获取所有在线用户信息
     * @return 在线用户信息 Map
     */
    public Map<String, UserDTO> getAllOnlineUsers() {
        Map<String, UserDTO> users = new ConcurrentHashMap<>();
        Set<String> userIds = getAllOnlineUserIds();
        
        for (String userId : userIds) {
            UserDTO userDTO = getOnlineUser(userId);
            if (userDTO != null) {
                users.put(userId, userDTO);
            }
        }
        return users;
    }
    
    /**
     * 获取在线用户数量
     * @return 在线用户数
     */
    public long getOnlineCount() {
        return getAllOnlineUserIds().size();
    }
}
