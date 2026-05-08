package com.fish.chat.common.constants;

/**
 * 认证相关常量
 */
public final class AuthConstants {
    
    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Token Header 名称
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * WebSocket Token 参数名称
     */
    public static final String WS_TOKEN_PARAM = "token";
    
    /**
     * 用户 ID Session 属性名称
     */
    public static final String USER_CODE_ATTR = "userCode";
    
    /**
     * 用户名 Session 属性名称
     */
    public static final String USERNAME_ATTR = "username";
    
    /**
     * Redis 在线用户 Key 前缀
     */
    public static final String ONLINE_USER_PREFIX = "user:online:";
    
    /**
     * 在线用户过期时间（分钟）
     */
    public static final long ONLINE_USER_EXPIRE_MINUTES = 1L;
    
    private AuthConstants() {
        // 防止实例化
    }
}
