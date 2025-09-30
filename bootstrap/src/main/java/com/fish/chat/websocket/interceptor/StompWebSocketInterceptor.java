package com.fish.chat.websocket.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import java.util.List;
import java.util.Map;
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
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

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
                    log.info("用户 {} STOMP连接成功", loginId);
                } catch (Exception e) {
                    log.warn("未授权客户端，STOMP连接失败: {}", e.getMessage());
                    return null; // 拒绝连接
                }
            } else {
                log.warn("STOMP连接缺少token参数");
                return null; // 拒绝连接
            }
        }

        return message;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从参数中获取token
        String token = getTokenFromRequest(request);

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket握手失败: 缺少token参数");
            return false;
        }

        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            // 标记 userId，握手成功
            attributes.put("userId", loginId);
            log.info("用户 {} WebSocket握手成功", loginId);
            return true;
        } catch (Exception e) {
            log.warn("未授权客户端，WebSocket握手失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Exception exception) {
        // 握手之后触发
    }

    /**
     * 从请求中提取token
     *
     * @param request ServerHttpRequest对象
     * @return token字符串，如果不存在则返回null
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.substring(query.indexOf("token=") + 6);
            if (token.contains("&")) {
                token = token.substring(0, token.indexOf("&"));
            }
            return token;
        }
        return null;
    }
}