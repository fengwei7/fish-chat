package com.fish.chat.websocket.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
            log.info("处理STOMP CONNECT命令");

            // 每次发送消息时鉴权

            // 获取连接中的token
            List<String> tokenHeaders = accessor.getNativeHeader("token");
            String token = null;
            
            if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                token = tokenHeaders.get(0);
                log.info("从header中获取到token: {}", token);
            }

            // 如果从header中没有获取到token，尝试从session中获取
            if (token == null && accessor.getSessionAttributes() != null) {
                Object sessionToken = accessor.getSessionAttributes().get("token");
                if (sessionToken instanceof String) {
                    token = (String) sessionToken;
                    log.info("从session中获取到token: {}", token);
                }
            }

            if (token != null) {
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
        log.info("WebSocket握手开始: {}", request.getURI());
        
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
            attributes.put("token", token); // 保存token到session属性中
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
        log.info("WebSocket握手完成");
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
            // URL解码token
            try {
                token = URLDecoder.decode(token, StandardCharsets.UTF_8.toString());
            } catch (Exception e) {
                log.warn("Token URL解码失败: {}", e.getMessage());
            }
            log.info("从请求中解析到token: {}", token);
            return token;
        }
        return null;
    }
}