package com.fish.chat.core.chat;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户会话 — 封装一个 WebSocket 连接
 */
@Data
public class ChatSession {

    /** 用户 code */
    private final String userId;

    /** 用户名 */
    private final String username;

    /** 用户头像 */
    private final String avatarUrl;

    /** Netty Channel */
    private final Channel channel;

    /** 连接时间 */
    private final long connectTime;

    /** 用户已加入的房间ID集合 */
    private final Set<String> joinedRooms;

    /** 最后活跃时间 */
    private volatile long lastActiveTime;

    public ChatSession(String userId, String username, String avatarUrl, Channel channel) {
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.channel = channel;
        this.connectTime = System.currentTimeMillis();
        this.lastActiveTime = System.currentTimeMillis();
        this.joinedRooms = ConcurrentHashMap.newKeySet();
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    public void touch() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public void joinRoom(String roomId) {
        joinedRooms.add(roomId);
    }

    public void leaveRoom(String roomId) {
        joinedRooms.remove(roomId);
    }

    public boolean isInRoom(String roomId) {
        return joinedRooms.contains(roomId);
    }

    /**
     * 向该会话发送消息文本（JSON）
     */
    public void sendText(String json) {
        if (isActive()) {
            channel.writeAndFlush(new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(json));
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (isActive()) {
            channel.close();
        }
    }
}
