package com.fish.chat.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler(), "/websocket/chat")
                .addInterceptors(webSocketInterceptor())
                .setAllowedOrigins("*");
    }
    
    @Bean
    public WebSocketHandler webSocketHandler() {
        return new ChatWebSocketHandler();
    }
    
    @Bean
    public HandshakeInterceptor webSocketInterceptor() {
        return new WebSocketInterceptor();
    }
}
