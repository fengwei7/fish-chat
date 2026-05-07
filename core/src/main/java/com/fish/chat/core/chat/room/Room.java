package com.fish.chat.core.chat.room;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天房间抽象 — 单聊/群聊/频道的统一模型
 */
@Data
@AllArgsConstructor
public class Room {

    /** 房间唯一ID */
    private final String roomId;

    /** 房间类型 */
    private final RoomType type;

    /** 房间名称 */
    private String name;

    /** 房间头像 */
    private String avatar;

    /** 成员列表（线程安全） */
    private final Set<String> members;

    /** 是否持久化到DB（群组/频道为true，私聊为false） */
    private final boolean persistent;

    public Room(String roomId, RoomType type, String name) {
        this(roomId, type, name, null, ConcurrentHashMap.newKeySet(), false);
    }

    public Room(String roomId, RoomType type, String name, String avatar, boolean persistent) {
        this.roomId = roomId;
        this.type = type;
        this.name = name;
        this.avatar = avatar;
        this.members = ConcurrentHashMap.newKeySet();
        this.persistent = persistent;
    }

    // ==================== 成员管理 ====================

    public boolean addMember(String userId) {
        return members.add(userId);
    }

    public boolean removeMember(String userId) {
        return members.remove(userId);
    }

    public boolean isMember(String userId) {
        return members.contains(userId);
    }

    public int memberCount() {
        return members.size();
    }

    public Set<String> getOnlineMembers(java.util.function.Predicate<String> isOnline) {
        // 不可变快照
        Set<String> online = ConcurrentHashMap.newKeySet();
        for (String member : members) {
            if (isOnline.test(member)) {
                online.add(member);
            }
        }
        return Collections.unmodifiableSet(online);
    }

    // ==================== 工厂方法 ====================

    /**
     * 私聊房间 — roomId 由两个用户 code 按字典序拼接生成
     * 示例：private:abc123:def456
     */
    public static String buildPrivateRoomId(String userCode1, String userCode2) {
        if (userCode1.compareTo(userCode2) < 0) {
            return "private:" + userCode1 + ":" + userCode2;
        }
        return "private:" + userCode2 + ":" + userCode1;
    }

    public static Room createPrivate(String roomId) {
        return new Room(roomId, RoomType.PRIVATE, "", null, false);
    }

    public static Room createGroup(String groupCode, String name, String avatar, Set<String> members) {
        Room room = new Room("group:" + groupCode, RoomType.GROUP, name, avatar, true);
        if (members != null) {
            room.members.addAll(members);
        }
        return room;
    }

    public static Room createChannel(String channelCode, String name, String avatar, Set<String> members) {
        Room room = new Room("channel:" + channelCode, RoomType.CHANNEL, name, avatar, true);
        if (members != null) {
            room.members.addAll(members);
        }
        return room;
    }
}
