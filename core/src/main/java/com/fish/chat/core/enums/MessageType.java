package com.fish.chat.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@AllArgsConstructor
@Getter
public enum MessageType {

    /** 文本消息 */
    TEXT("TEXT", "文本"),
    /** 图片消息 */
    IMAGE("IMAGE", "图片"),
    /** 文件消息 */
    FILE("FILE", "文件"),
    /** 系统消息 */
    SYSTEM("SYSTEM", "系统");

    private final String value;
    private final String desc;
}