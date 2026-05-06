package com.fish.chat.core.netty;

import io.netty.util.AttributeKey;

/**
 * Netty Channel 属性键定义
 */
public class NettyChannelAttributes {

    /**
     * 用户 ID 属性键
     */
    public static final AttributeKey<String> USER_ID = AttributeKey.valueOf("userId");

    private NettyChannelAttributes() {
        // 防止实例化
    }
}
