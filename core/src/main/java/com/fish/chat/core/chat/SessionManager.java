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

        // 保存到 Redis（在线状态标记）
        UserDTO dto = new UserDTO();
        dto.setCode(userCode);
        dto.setUsername(username);
        dto.setAvatarUrl(avatarUrl);
        dto.setOnline(true);
        redisUtil.set(buildOnlineKey(userCode), JSON.toJSONString(dto), AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("用户 {} 上线，当前在线：{}", userCode, sessions.size());
        return session;
    }

    /**
     * 注销会话
     */
    public void unregister(String userCode) {
        ChatSession session = sessions.remove(userCode);
        if (session != null) {
            session.close();
            redisUtil.deleteByKey(buildOnlineKey(userCode));
            log.info("用户 {} 下线，当前在线：{}", userCode, sessions.size());
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
    public void broadcastToRoom(Set<String> memberCodes, ChatMessagePacket packet, String excludeUserCode) {
        String json = JSON.toJSONString(packet);
        broadcastToRoomRaw(memberCodes, json, excludeUserCode);
    }

    /**
     * 向房间内所有在线成员广播JSON（排除指定用户）
     */
    public void broadcastToRoomRaw(Set<String> memberCodes, String json, String excludeUserCode) {
        for (String memberCode : memberCodes) {
            if (excludeUserCode != null && excludeUserCode.equals(memberCode)) {
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

    public void refreshOnlineStatus(String userCode) {
        redisUtil.expire(buildOnlineKey(userCode), AuthConstants.ONLINE_USER_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    public boolean isOnlineInRedis(String userCode) {
        return redisUtil.hasKey(buildOnlineKey(userCode));
    }

    private String buildOnlineKey(String userCode) {
        return AuthConstants.ONLINE_USER_PREFIX + userCode;
    }
}
