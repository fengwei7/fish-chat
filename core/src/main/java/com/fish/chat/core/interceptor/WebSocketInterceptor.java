package com.fish.chat.core.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 验证 Token 并将用户 ID 传入会话
 */
public class WebSocketInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从 URL 参数获取 token
        String token = null;
        if (request.getURI().getQuery() != null) {
            String[] params = request.getURI().getQuery().split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if ("token".equals(pair[0])) {
                    token = pair[1];
                    break;
                }
            }
        }
        
        if (token == null) {
            return false;
        }
        
        // 验证 token 并获取用户 ID
        try {
            // Sa-Token 1.44.0: 通过 token 获取登录 ID
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId != null && !"undefined".equals(loginId.toString())) {
                attributes.put("userId", loginId.toString());
                return true;
            }
            return false;
        } catch (Exception e) {
            // Token 无效或已过期
            return false;
        }
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后处理，可以留空
    }
}
