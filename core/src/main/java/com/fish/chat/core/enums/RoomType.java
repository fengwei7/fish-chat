package com.fish.chat.core.enums;

import lombok.AllArgsConstructor;import lombok.Getter;

/**
 * 房间类型
 */
@AllArgsConstructor
@Getter
public enum RoomType {

    /** 单聊（1v1） */
    PRIVATE("private", "单聊"),
    /** 群聊 */
    GROUP("group", "群聊"),
    /** 频道（一对多广播） */
    CHANNEL("channel", "频道");

    private final String value;
    
    private final String desc;
}
