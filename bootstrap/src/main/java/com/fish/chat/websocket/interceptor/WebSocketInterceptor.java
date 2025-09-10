package com.fish.chat.websocket.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import cn.dev33.satoken.stp.StpUtil;

/**
 * WebSocket 握手的前置拦截器
 */
public class WebSocketInterceptor implements HandshakeInterceptor {

    // 握手之前触发 (return true 才会握手成功 )
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
                                   Map<String, Object> attributes) throws Exception {
        
        System.out.println("---- 握手之前触发");
        
        // 从参数中获取token
        String query = request.getURI().getQuery();
        String token = null;
        if (query != null && query.contains("token=")) {
            token = query.substring(query.indexOf("token=") + 6);
            if (token.contains("&")) {
                token = token.substring(0, token.indexOf("&"));
            }
        }

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            // 标记 userId，握手成功
            attributes.put("userId", loginId);
            return true;
        } catch (Exception e) {
            System.out.println("---- 未授权客户端，连接失败: " + e.getMessage());
            return false;
        }
    }

    // 握手之后触发
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        System.out.println("---- 握手之后触发 ");
    }

}