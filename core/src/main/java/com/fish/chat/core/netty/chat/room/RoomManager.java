package com.fish.chat.core.netty.chat.room;

import com.fish.chat.core.enums.RoomType;
import com.fish.chat.core.netty.chat.SessionManager;
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

    /** roomCode -> Room（所有房间都在内存中，持久化房间从DB加载） */
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();

    @Resource
    private SessionManager sessionManager;

    // ==================== 获取/创建房间 ====================

    /**
     * 获取或创建私聊房间
     */
    public Room getOrCreatePrivateRoom(String userCode1, String userCode2) {
        String roomCode = Room.buildPrivateRoomCode(userCode1, userCode2);
        return rooms.computeIfAbsent(roomCode, id -> {
            Room room = Room.createPrivate(roomCode);
            room.addMember(userCode1);
            room.addMember(userCode2);
            log.info("创建私聊房间: {}", roomCode);
            return room;
        });
    }

    /**
     * 获取或创建群聊房间（从DB加载成员）
     */
    public Room getOrCreateGroupRoom(String groupCode, String groupName, String groupAvatar, Set<String> memberCodes) {
        String roomCode = RoomType.GROUP.getValue() + ":" + groupCode;
        return rooms.computeIfAbsent(roomCode, id -> {
            Room room = Room.createGroup(groupCode, groupName, groupAvatar, memberCodes);
            log.info("创建群聊房间: {}，成员: {}", roomCode, memberCodes != null ? memberCodes.size() : 0);
            return room;
        });
    }

    /**
     * 获取或创建频道房间（从DB加载成员）
     */
    public Room getOrCreateChannelRoom(String channelCode, String channelName, String channelAvatar, Set<String> memberCodes) {
        String roomCode = RoomType.CHANNEL.getValue() + ":" + channelCode;
        return rooms.computeIfAbsent(roomCode, id -> {
            Room room = Room.createChannel(channelCode, channelName, channelAvatar, memberCodes);
            log.info("创建频道房间: {}，订阅者: {}", roomCode, memberCodes != null ? memberCodes.size() : 0);
            return room;
        });
    }

    /**
     * 根据 roomCode 获取房间
     */
    public Room getRoom(String roomCode) {
        return rooms.get(roomCode);
    }

    // ==================== 成员管理 ====================

    /**
     * 用户加入房间
     */
    public boolean joinRoom(String roomCode, String userCode) {
        Room room = rooms.get(roomCode);
        if (room == null) return false;
        boolean added = room.addMember(userCode);
        if (added) {
            // 更新会话中的房间列表
            if (sessionManager.get(userCode) != null) {
                sessionManager.get(userCode).joinRoom(roomCode);
            }
        }
        return added;
    }

    /**
     * 用户离开房间
     */
    public boolean leaveRoom(String roomCode, String userCode) {
        Room room = rooms.get(roomCode);
        if (room == null) return false;
        boolean removed = room.removeMember(userCode);
        if (removed && sessionManager.get(userCode) != null) {
            sessionManager.get(userCode).leaveRoom(roomCode);
        }
        return removed;
    }

    /**
     * 向房间添加新成员（群聊加人时）
     */
    public void addMemberToGroup(String groupCode, String userCode) {
        String roomCode = RoomType.GROUP.getValue() + ":" + groupCode;
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.addMember(userCode);
            if (sessionManager.get(userCode) != null) {
                sessionManager.get(userCode).joinRoom(roomCode);
            }
        }
    }

    /**
     * 从房间移除成员（群聊踢人时）
     */
    public void removeMemberFromGroup(String groupCode, String userCode) {
        String roomCode = RoomType.GROUP.getValue() + ":" + groupCode;
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.removeMember(userCode);
            if (sessionManager.get(userCode) != null) {
                sessionManager.get(userCode).leaveRoom(roomCode);
            }
        }
    }
    
    /**
     * 更新群组信息（名称、头像）
     */
    public void updateGroupInfo(String groupCode, String name, String avatar) {
        String roomCode = RoomType.GROUP.getValue() + ":" + groupCode;
        Room room = rooms.get(roomCode);
        if (room != null) {
            if (name != null) {
                room.setName(name);
            }
            if (avatar != null) {
                room.setAvatar(avatar);
            }
            log.info("更新群组房间信息: {}", roomCode);
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
                log.info("清理空私聊房间: {}", room.getRoomCode());
                return true;
            }
            return false;
        });
    }
}
