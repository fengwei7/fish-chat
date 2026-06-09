package com.fish.chat.core.repository;

import com.alibaba.fastjson2.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.common.redisutils.RedisUtil;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.UserDTO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    public void saveOnlineUser(String userCode, UserDTO userDTO, long expireMinutes) {
        redisUtil.set(buildKey(userCode), toJson(userDTO), expireMinutes, TimeUnit.MINUTES);
    }

    /**
     * 更新在线用户过期时间
     */
    public void updateOnlineUserExpire(String userCode, long expireMinutes) {
        redisUtil.expire(buildKey(userCode), expireMinutes, TimeUnit.MINUTES);
    }

    /**
     * 删除在线用户
     */
    public void removeOnlineUser(String userCode) {
        redisUtil.deleteByKey(buildKey(userCode));
    }

    /**
     * 获取在线用户信息
     */
    public UserDTO getOnlineUser(String userCode) {
        Object value = redisUtil.getByKey(buildKey(userCode));
        return value == null ? null : parseUser((String) value);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(String userCode) {
        return redisUtil.hasKey(buildKey(userCode));
    }

    /**
     * 获取所有在线用户 ID 列表（使用 SCAN 避免阻塞 Redis）
     */
    public Set<String> getAllOnlineUserCodes() {
        Set<String> keys = redisUtil.scanKeys(buildKey("*"));

        Set<String> userCodes = new HashSet<>(keys.size());
        for (String key : keys) {
            userCodes.add(extractUserCode(key));
        }
        return userCodes;
    }

    /**
     * 获取所有在线用户信息
     */
    public Map<String, UserDTO> getAllOnlineUsers() {
        Set<String> userCodes = getAllOnlineUserCodes();
        Map<String, UserDTO> users = new HashMap<>(Math.max(userCodes.size(), 1));

        for (String userCode : userCodes) {
            UserDTO userDTO = getOnlineUser(userCode);
            if (userDTO != null) {
                users.put(userCode, userDTO);
            }
        }
        return users;
    }

    /**
     * 获取在线用户数量
     */
    public long getOnlineCount() {
        return getAllOnlineUserCodes().size();
    }

    /**
     * 分页获取在线用户 code 列表
     */
    public PageResult<String> getOnlineUserPage(int pageNum, int pageSize) {
        Set<String> onlineIds = getAllOnlineUserCodes();
        List<String> list = new ArrayList<>(onlineIds);
        return PageResult.ofPage(list, pageNum, pageSize);
    }

    // ========== 私有辅助方法 ==========

    private String buildKey(String userCode) {
        return AuthConstants.ONLINE_USER_PREFIX + userCode;
    }

    private String extractUserCode(String key) {
        return key.substring(AuthConstants.ONLINE_USER_PREFIX.length());
    }

    private String toJson(UserDTO userDTO) {
        return JSON.toJSONString(userDTO);
    }

    private UserDTO parseUser(String json) {
        return JSON.parseObject(json, UserDTO.class);
    }
}
