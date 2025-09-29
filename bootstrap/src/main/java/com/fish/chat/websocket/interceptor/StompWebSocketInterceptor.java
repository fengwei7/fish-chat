package com.fish.chat.websocket.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StompWebSocketInterceptor implements ChannelInterceptor, HandshakeInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 获取连接中的token
            List<String> tokenHeaders = accessor.getNativeHeader("token");
            if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                String token = tokenHeaders.get(0);
                
                try {
                    Object loginId = StpUtil.getLoginIdByToken(token);
                    // 将用户ID添加到会话属性中
                    accessor.setUser(() -> String.valueOf(loginId));
                    log.info("用户 " + loginId + " STOMP连接成功");
                } catch (Exception e) {
                    log.info("未授权客户端，STOMP连接失败: " + e.getMessage());
                    return null; // 拒绝连接
                }
            } else {
                log.info("STOMP连接缺少token参数");
                return null; // 拒绝连接
            }
        }
        
        return message;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
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
            log.warn("未授权客户端，连接失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手之后触发
    }
}