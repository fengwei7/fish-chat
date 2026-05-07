package com.fish.chat.core.chat;

import com.alibaba.fastjson.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.common.redisutils.RedisUtil;
import com.fish.chat.core.entity.dto.UserDTO;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    /** userId -> ChatSession */
    private final ConcurrentHashMap<String, ChatSession> sessions = new ConcurrentHashMap<>();

    @Resource
    private RedisUtil redisUtil;

    @PostConstruct
    public void init() {
        log.info("SessionManager 初始化完成");
    }

    // ==================== 注册/注销 ====================

    /**
     * 注册新会话（断旧连新）
     */
    public ChatSession register(String userId, String username, String avatarUrl, Channel channel) {
        // 如果已有连接，先踢掉旧连接
        ChatSession old = sessions.get(userId);
        if (old != null && old.isActive()) {
            log.info("用户 {} 已有连接，关闭旧连接", userId);
            old.sendText(JSON.toJSONString(ChatMessagePacket.notify("SYSTEM", "您的账号在其他设备登录")));
            old.close();
        }

        ChatSession session = new ChatSession(userId, username, avatarUrl, channel);
        sessions.put(userId, session);

        // 保存到 Redis（在线状态标记）
        UserDTO dto = new UserDTO();
        dto.setCode(userId);
        dto.setUsername(username);
        dto.setAvatarUrl(avatarUrl);
        dto.setOnline(true);
        redisUtil.set(buildOnlineKey(userId), JSON.toJSONString(dto), AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("用户 {} 上线，当前在线：{}", userId, sessions.size());
        return session;
    }

    /**
     * 注销会话
     */
    public void unregister(String userId) {
        ChatSession session = sessions.remove(userId);
        if (session != null) {
            session.close();
            redisUtil.deleteByKey(buildOnlineKey(userId));
            log.info("用户 {} 下线，当前在线：{}", userId, sessions.size());
        }
    }

    // ==================== 查询 ====================

    public ChatSession get(String userId) {
        return sessions.get(userId);
    }

    public boolean isOnline(String userId) {
        ChatSession session = sessions.get(userId);
        return session != null && session.isActive();
    }

    public int onlineCount() {
        return sessions.size();
    }

    public Set<String> getAllOnlineUserIds() {
        return sessions.keySet();
    }

    // ==================== 消息发送 ====================

    /**
     * 向指定用户发消息
     */
    public void sendToUser(String userId, ChatMessagePacket packet) {
        sendToUserRaw(userId, JSON.toJSONString(packet));
    }

    /**
     * 向指定用户发JSON字符串
     */
    public void sendToUserRaw(String userId, String json) {
        ChatSession session = sessions.get(userId);
        if (session != null && session.isActive()) {
            session.sendText(json);
        }
    }

    /**
     * 向房间内所有在线成员广播（排除指定用户）
     */
    public void broadcastToRoom(Set<String> memberIds, ChatMessagePacket packet, String excludeUserId) {
        String json = JSON.toJSONString(packet);
        broadcastToRoomRaw(memberIds, json, excludeUserId);
    }

    /**
     * 向房间内所有在线成员广播JSON（排除指定用户）
     */
    public void broadcastToRoomRaw(Set<String> memberIds, String json, String excludeUserId) {
        for (String memberId : memberIds) {
            if (excludeUserId != null && excludeUserId.equals(memberId)) {
                continue;
            }
            sendToUserRaw(memberId, json);
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

    public void refreshOnlineStatus(String userId) {
        redisUtil.expire(buildOnlineKey(userId), AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    public boolean isOnlineInRedis(String userId) {
        return redisUtil.hasKey(buildOnlineKey(userId));
    }

    private String buildOnlineKey(String userId) {
        return AuthConstants.ONLINE_USER_PREFIX + userId;
    }
}
