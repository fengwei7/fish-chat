package com.fish.chat.core.chat.room;

import com.fish.chat.core.chat.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间管理器 — 管理所有聊天房间
 */
@Slf4j
@Component
public class RoomManager {

    /** roomId -> Room（所有房间都在内存中，持久化房间从DB加载） */
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    @Resource
    private SessionManager sessionManager;

    // ==================== 获取/创建房间 ====================

    /**
     * 获取或创建私聊房间
     */
    public Room getOrCreatePrivateRoom(String userCode1, String userCode2) {
        String roomId = Room.buildPrivateRoomId(userCode1, userCode2);
        return rooms.computeIfAbsent(roomId, id -> {
            Room room = Room.createPrivate(roomId);
            room.addMember(userCode1);
            room.addMember(userCode2);
            log.info("创建私聊房间: {}", roomId);
            return room;
        });
    }

    /**
     * 获取或创建群聊房间（从DB加载成员）
     */
    public Room getOrCreateGroupRoom(String groupCode, String groupName, String groupAvatar, Set<String> memberIds) {
        String roomId = "group:" + groupCode;
        return rooms.computeIfAbsent(roomId, id -> {
            Room room = Room.createGroup(groupCode, groupName, groupAvatar, memberIds);
            log.info("创建群聊房间: {}，成员: {}", roomId, memberIds.size());
            return room;
        });
    }

    /**
     * 获取或创建频道房间（从DB加载成员）
     */
    public Room getOrCreateChannelRoom(String channelCode, String channelName, String channelAvatar, Set<String> memberIds) {
        String roomId = "channel:" + channelCode;
        return rooms.computeIfAbsent(roomId, id -> {
            Room room = Room.createChannel(channelCode, channelName, channelAvatar, memberIds);
            log.info("创建频道房间: {}，订阅者: {}", roomId, memberIds.size());
            return room;
        });
    }

    /**
     * 根据 roomId 获取房间
     */
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    // ==================== 成员管理 ====================

    /**
     * 用户加入房间
     */
    public boolean joinRoom(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) return false;
        boolean added = room.addMember(userId);
        if (added) {
            // 更新会话中的房间列表
            if (sessionManager.get(userId) != null) {
                sessionManager.get(userId).joinRoom(roomId);
            }
        }
        return added;
    }

    /**
     * 用户离开房间
     */
    public boolean leaveRoom(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) return false;
        boolean removed = room.removeMember(userId);
        if (removed && sessionManager.get(userId) != null) {
            sessionManager.get(userId).leaveRoom(roomId);
        }
        return removed;
    }

    /**
     * 向房间添加新成员（群聊加人时）
     */
    public void addMemberToGroup(String groupCode, String userId) {
        String roomId = "group:" + groupCode;
        Room room = rooms.get(roomId);
        if (room != null) {
            room.addMember(userId);
            if (sessionManager.get(userId) != null) {
                sessionManager.get(userId).joinRoom(roomId);
            }
        }
    }

    /**
     * 从房间移除成员（群聊踢人时）
     */
    public void removeMemberFromGroup(String groupCode, String userId) {
        String roomId = "group:" + groupCode;
        Room room = rooms.get(roomId);
        if (room != null) {
            room.removeMember(userId);
            if (sessionManager.get(userId) != null) {
                sessionManager.get(userId).leaveRoom(roomId);
            }
        }
    }

    // ==================== 查询 ====================

    public int roomCount() {
        return rooms.size();
    }

    /**
     * 移除空房间（私聊房间无人时清理）
     */
    public void evictEmptyRooms() {
        rooms.entrySet().removeIf(entry -> {
            Room room = entry.getValue();
            if (room.getType() == RoomType.PRIVATE && room.memberCount() == 0) {
                log.info("清理空私聊房间: {}", room.getRoomId());
                return true;
            }
            return false;
        });
    }
}
