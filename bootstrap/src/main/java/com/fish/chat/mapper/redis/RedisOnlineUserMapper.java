package com.fish.chat.mapper.redis;

import com.alibaba.fastjson.JSON;
import com.fish.chat.dto.UserDTO;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis在线用户Mapper
 */
@Repository
public class RedisOnlineUserMapper {

    private static final String ONLINE_USER_PREFIX = "user:online:";
    private final StringRedisTemplate stringRedisTemplate;

    public RedisOnlineUserMapper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 保存用户在线信息
     *
     * @param userId 用户ID
     * @param userDTO 用户信息
     * @param expireMinutes 过期时间（分钟）
     */
    public void saveOnlineUser(String userId, UserDTO userDTO, long expireMinutes) {
        stringRedisTemplate.opsForValue().set(
            ONLINE_USER_PREFIX + userId,
            JSON.toJSONString(userDTO),
            Duration.ofMinutes(expireMinutes)
        );
    }

    /**
     * 删除用户在线信息
     *
     * @param userId 用户ID
     */
    public void removeOnlineUser(String userId) {
        stringRedisTemplate.delete(ONLINE_USER_PREFIX + userId);
    }

    /**
     * 获取所有在线用户
     *
     * @return 在线用户映射
     */
    public Map<String, UserDTO> getAllOnlineUsers() {
        Map<String, UserDTO> onlineUsers = new ConcurrentHashMap<>();
        Set<String> keys = stringRedisTemplate.keys(ONLINE_USER_PREFIX + "*");

        if (keys != null) {
            for (String key : keys) {
                String userData = stringRedisTemplate.opsForValue().get(key);
                if (userData != null) {
                    try {
                        UserDTO userDTO = JSON.parseObject(userData, UserDTO.class);
                        // 从key中提取userId (移除"user:online:"前缀)
                        String userId = key.substring(ONLINE_USER_PREFIX.length());
                        onlineUsers.put(userId, userDTO);
                    } catch (Exception e) {
                        // 解析失败则跳过该用户
                    }
                }
            }
        }
        return onlineUsers;
    }

    /**
     * 更新用户在线状态过期时间
     *
     * @param userId 用户ID
     * @param expireMinutes 过期时间（分钟）
     */
    public void updateOnlineUserExpire(String userId, long expireMinutes) {
        stringRedisTemplate.expire(ONLINE_USER_PREFIX + userId, Duration.ofMinutes(expireMinutes));
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数量
     */
    public long getOnlineUserCount() {
        Set<String> keys = stringRedisTemplate.keys(ONLINE_USER_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }
}