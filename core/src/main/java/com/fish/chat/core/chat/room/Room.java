package com.fish.chat.core.chat.room;

import com.fish.chat.core.enums.RoomType;
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
    private final String roomCode;

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

    public Room(String roomCode, RoomType type, String name) {
        this(roomCode, type, name, null, ConcurrentHashMap.newKeySet(), false);
    }

    public Room(String roomCode, RoomType type, String name, String avatar, boolean persistent) {
        this.roomCode = roomCode;
        this.type = type;
        this.name = name;
        this.avatar = avatar;
        this.members = ConcurrentHashMap.newKeySet();
        this.persistent = persistent;
    }

    // ==================== 成员管理 ====================

    public boolean addMember(String userCode) {
        return members.add(userCode);
    }

    public boolean removeMember(String userCode) {
        return members.remove(userCode);
    }

    public boolean isMember(String userCode) {
        return members.contains(userCode);
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
     * 私聊房间 — roomCode 由两个用户 code 按字典序拼接生成
     * 示例：private:abc123:def456
     */
    public static String buildPrivateRoomCode(String userCode1, String userCode2) {
        if (userCode1.compareTo(userCode2) < 0) {
            return RoomType.PRIVATE.getValue() + ":" + userCode1 + ":" + userCode2;
        }
        return RoomType.PRIVATE.getValue() + ":" + userCode2 + ":" + userCode1;
    }

    public static Room createPrivate(String roomCode) {
        return new Room(roomCode, RoomType.PRIVATE, "", null, false);
    }

    public static Room createGroup(String groupCode, String name, String avatar, Set<String> members) {
        Room room = new Room(RoomType.GROUP.getValue() + ":" + groupCode, RoomType.GROUP, name, avatar, true);
        if (members != null) {
            room.members.addAll(members);
        }
        return room;
    }

    public static Room createChannel(String channelCode, String name, String avatar, Set<String> members) {
        Room room = new Room(RoomType.CHANNEL.getValue() + ":" + channelCode, RoomType.CHANNEL, name, avatar, true);
        if (members != null) {
            room.members.addAll(members);
        }
        return room;
    }
}
