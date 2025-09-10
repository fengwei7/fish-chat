package com.fish.chat.config;

import com.fish.chat.websocket.handler.ChatWebSocketHandler;
import com.fish.chat.websocket.interceptor.WebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket处理器配置
 */
@Configuration
@EnableWebSocket
public class WebSocketHandlerConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器和拦截器
        registry.addHandler(chatWebSocketHandler(), "/websocket/chat")
                .addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");
    }

    /**
     * WebSocket处理器
     * @return ChatWebSocketHandler
     */
    public ChatWebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }

    /**
     * WebSocket拦截器
     * @return WebSocketInterceptor
     */
    public WebSocketInterceptor webSocketInterceptor() {
        return new WebSocketInterceptor();
    }
}