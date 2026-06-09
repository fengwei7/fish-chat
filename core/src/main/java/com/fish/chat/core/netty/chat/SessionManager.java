package com.fish.chat.core.netty.chat;

import com.alibaba.fastjson2.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.common.redisutils.RedisUtil;
import com.fish.chat.core.entity.dto.UserDTO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 会话管理器 — 管理所有在线用户连接
 */
@Slf4j
@Component
public class SessionManager {

    /** userCode -> ChatSession */
    private final ConcurrentHashMap<String, ChatSession> sessions = new ConcurrentHashMap<>();

    @Resource
    private RedisUtil redisUtil;

    /**
     * 初始化会话管理器
     * 由 ApplicationStartupManager 统一调用
     */
    public void init() {
        log.info("SessionManager 初始化完成");
        cleanDirtyData();
    }

    /**
     * 清理 Redis 中的脏数据
     * 应用启动时执行，确保 Redis Set 和详细数据一致
     * 
     * 脏数据场景：
     * 1. Set 中有 userCode，但详细数据已过期（服务器宕机、Redis 重启等）
     * 2. 详细数据存在，但 Set 中没有（极少见，可能是手动操作 Redis）
     */
    private void cleanDirtyData() {
        log.info("开始清理 Redis 中的脏数据...");
        
        // 获取 Set 中的所有在线用户
        Set<String> onlineUsersInSet = redisUtil.getSetMembers(AuthConstants.ONLINE_USERS_SET);
        int cleanedCount = 0;
        
        // 清理场景 1：Set 中有，但详细数据不存在
        for (String userCode : onlineUsersInSet) {
            if (!redisUtil.hasKey(buildOnlineKey(userCode))) {
                redisUtil.removeFromSet(AuthConstants.ONLINE_USERS_SET, userCode);
                cleanedCount++;
                log.debug("清理脏数据: userCode={} (Set 中有但详细数据不存在)", userCode);
            }
        }
        
        log.info("Redis 脏数据清理完成，共清理 {} 条脏数据，全局在线用户：{}", 
                cleanedCount, redisUtil.getSetMembers(AuthConstants.ONLINE_USERS_SET).size());
    }

    // ==================== 注册/注销 ====================

    /**
     * 注册新会话（断旧连新）
     */
    public ChatSession register(String userCode, String username, String avatarUrl, Channel channel) {
        // 如果已有连接，先踢掉旧连接
        ChatSession old = sessions.get(userCode);
        if (old != null && old.isActive()) {
            log.info("用户 {} 已有连接，关闭旧连接", userCode);
            old.sendText(JSON.toJSONString(ChatMessagePacket.notify("SYSTEM", "您的账号在其他设备登录")));
            old.close();
        }

        ChatSession session = new ChatSession(userCode, username, avatarUrl, channel);
        sessions.put(userCode, session);

        // 保存到 Redis（在线状态标记）- 先保存详细数据
        UserDTO dto = new UserDTO();
        dto.setCode(userCode);
        dto.setUsername(username);
        dto.setAvatarUrl(avatarUrl);
        dto.setOnline(true);
        boolean detailSaved = redisUtil.set(buildOnlineKey(userCode), JSON.toJSONString(dto), 
                AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        if (detailSaved) {
            // 详细数据保存成功后，再添加到 Set 索引
            redisUtil.addToSet(AuthConstants.ONLINE_USERS_SET, userCode);
        } else {
            log.error("用户 {} 上线失败：Redis 详细数据保存失败", userCode);
            // 清理内存中的 session
            sessions.remove(userCode);
            session.close();
            return null;
        }

        log.info("用户 {} 上线，当前节点在线：{}，全局在线：{}", 
                userCode, sessions.size(), globalOnlineCount());
        return session;
    }

    /**
     * 注销会话
     */
    public void unregister(String userCode) {
        ChatSession session = sessions.remove(userCode);
        if (session != null) {
            session.close();
            // 删除在线状态标记（详细数据）
            redisUtil.deleteByKey(buildOnlineKey(userCode));
            // 从 Redis Set 中移除（索引）
            redisUtil.removeFromSet(AuthConstants.ONLINE_USERS_SET, userCode);
            log.info("用户 {} 下线，当前节点在线：{}，全局在线：{}", 
                    userCode, sessions.size(), globalOnlineCount());
        }
    }

    // ==================== 查询 ====================

    public ChatSession get(String userCode) {
        return sessions.get(userCode);
    }

    public boolean isOnline(String userCode) {
        ChatSession session = sessions.get(userCode);
        return session != null && session.isActive();
    }

    /**
     * 获取所有在线用户 code（从 Redis Set 获取，支持分布式部署）
     */
    public Set<String> getAllOnlineUserCodes() {
        return redisUtil.getSetMembers(AuthConstants.ONLINE_USERS_SET);
    }

    /**
     * 获取内存中的在线用户数量（仅当前节点）
     */
    public int onlineCount() {
        return sessions.size();
    }

    /**
     * 获取全局在线用户数量（所有节点，从 Redis Set 获取）
     */
    public int globalOnlineCount() {
        return getAllOnlineUserCodes().size();
    }

    // ==================== 消息发送 ====================

    /**
     * 向指定用户发消息
     */
    public void sendToUser(String userCode, ChatMessagePacket packet) {
        sendToUserRaw(userCode, JSON.toJSONString(packet));
    }

    /**
     * 向指定用户发JSON字符串
     */
    public void sendToUserRaw(String userCode, String json) {
        ChatSession session = sessions.get(userCode);
        if (session != null && session.isActive()) {
            session.sendText(json);
        }
    }

    /**
     * 向房间内所有在线成员广播（排除指定用户）
     */
    public void broadcastToRoom(Set<String> memberCodes, ChatMessagePacket packet, Set<String> excludeUserCodes) {
        String json = JSON.toJSONString(packet);
        broadcastToRoomRaw(memberCodes, json, excludeUserCodes);
    }

    /**
     * 向房间内所有在线成员广播JSON（排除指定用户）
     */
    public void broadcastToRoomRaw(Set<String> memberCodes, String json, Set<String> excludeUserCodes) {
        for (String memberCode : memberCodes) {
            if (excludeUserCodes != null && excludeUserCodes.contains(memberCode)) {
                continue;
            }
            sendToUserRaw(memberCode, json);
        }
    }

    /**
     * 关闭所有连接（应用关闭时）
     */
    public void shutdown() {
        log.info("正在关闭所有连接，数量：{}", sessions.size());
        for (Map.Entry<String, ChatSession> entry : sessions.entrySet()) {
            ChatSession session = entry.getValue();
            session.sendText(JSON.toJSONString(ChatMessagePacket.notify("SYSTEM", "服务器维护中，请稍后重连")));
            session.close();
        }
        sessions.clear();
    }

    // ==================== Redis 在线状态 ====================

    /**
     * 刷新在线状态（续期）
     */
    public void refreshOnlineStatus(String userCode) {
        if (isOnline(userCode)) {
            // 刷新详细数据的过期时间
            redisUtil.expire(buildOnlineKey(userCode), AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);
            // 确保在 Set 中
            redisUtil.addToSet(AuthConstants.ONLINE_USERS_SET, userCode);
        }
    }

    /**
     * 定时任务：每分钟刷新所有在线用户的状态
     * 确保内存、Redis Set、Redis 详细数据的一致性
     * 
     * 清理逻辑：
     * 1. 从 Redis Set 获取所有在线用户
     * 2. 检查详细数据是否存在（作为真实在线的判断依据）
     * 3. 如果详细数据存在但不在内存中，说明是其他节点的用户，跳过
     * 4. 如果详细数据不存在，从 Set 中清理
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void scheduledRefreshOnlineStatus() {
        Set<String> onlineUserCodes = getAllOnlineUserCodes();
        int refreshedCount = 0;
        int cleanedCount = 0;
        
        for (String userCode : onlineUserCodes) {
            // 检查详细数据是否存在（真实在线的依据）
            if (redisUtil.hasKey(buildOnlineKey(userCode))) {
                // 仅刷新当前内存中存在的用户
                if (isOnline(userCode)) {
                    redisUtil.expire(buildOnlineKey(userCode), 
                            AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);
                    refreshedCount++;
                }
                // 如果不在内存中，说明是其他节点的用户，不需要刷新
            } else {
                // 详细数据不存在，从 Set 中清理
                redisUtil.removeFromSet(AuthConstants.ONLINE_USERS_SET, userCode);
                cleanedCount++;
                log.debug("定时清理脏数据: userCode={}", userCode);
            }
        }
        
        if (refreshedCount > 0 || cleanedCount > 0) {
            log.info("定时刷新在线状态完成，刷新 {} 个，清理 {} 个脏数据，全局在线：{}", 
                    refreshedCount, cleanedCount, globalOnlineCount());
        }
    }

    public boolean isOnlineInRedis(String userCode) {
        return redisUtil.hasKey(buildOnlineKey(userCode));
    }

    private String buildOnlineKey(String userCode) {
        return AuthConstants.ONLINE_USER_PREFIX + userCode;
    }
}
