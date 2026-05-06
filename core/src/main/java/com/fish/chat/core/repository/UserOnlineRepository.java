package com.fish.chat.core.repository;

import com.alibaba.fastjson.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.common.redisutils.RedisUtil;
import com.fish.chat.core.entity.dto.UserDTO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 在线用户仓储
 */
@Repository
public class UserOnlineRepository {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 保存在线用户到 Redis
     */
    public void saveOnlineUser(String userId, UserDTO userDTO, long expireMinutes) {
        redisUtil.set(buildKey(userId), toJson(userDTO), expireMinutes, TimeUnit.MINUTES);
    }

    /**
     * 更新在线用户过期时间
     */
    public void updateOnlineUserExpire(String userId, long expireMinutes) {
        redisUtil.expire(buildKey(userId), expireMinutes, TimeUnit.MINUTES);
    }

    /**
     * 删除在线用户
     */
    public void removeOnlineUser(String userId) {
        redisUtil.deleteByKey(buildKey(userId));
    }

    /**
     * 获取在线用户信息
     */
    public UserDTO getOnlineUser(String userId) {
        Object value = redisUtil.getByKey(buildKey(userId));
        return value == null ? null : parseUser((String) value);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(String userId) {
        return redisUtil.hasKey(buildKey(userId));
    }

    /**
     * 获取所有在线用户 ID 列表（使用 SCAN 避免阻塞 Redis）
     */
    public Set<String> getAllOnlineUserIds() {
        Set<String> keys = redisUtil.scanKeys(buildKey("*"));

        Set<String> userIds = new HashSet<>(keys.size());
        for (String key : keys) {
            userIds.add(extractUserId(key));
        }
        return userIds;
    }

    /**
     * 获取所有在线用户信息
     */
    public Map<String, UserDTO> getAllOnlineUsers() {
        Set<String> userIds = getAllOnlineUserIds();
        Map<String, UserDTO> users = new HashMap<>(Math.max(userIds.size(), 1));

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
     */
    public long getOnlineCount() {
        return getAllOnlineUserIds().size();
    }

    // ========== 私有辅助方法 ==========

    private String buildKey(String userId) {
        return AuthConstants.ONLINE_USER_PREFIX + userId;
    }

    private String extractUserId(String key) {
        return key.substring(AuthConstants.ONLINE_USER_PREFIX.length());
    }

    private String toJson(UserDTO userDTO) {
        return JSON.toJSONString(userDTO);
    }

    private UserDTO parseUser(String json) {
        return JSON.parseObject(json, UserDTO.class);
    }
}
