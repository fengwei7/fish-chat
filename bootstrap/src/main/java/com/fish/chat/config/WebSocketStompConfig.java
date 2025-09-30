package com.fish.chat.config;

import com.fish.chat.websocket.interceptor.StompWebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，客户端将通过这个端点进行连接
        registry.addEndpoint("/ws")
            .addInterceptors(new StompWebSocketInterceptor())
            .setAllowedOriginPatterns("*")
            .withSockJS(); // 使用SockJS，提供备选方案
        
        // 添加不使用SockJS的端点
        registry.addEndpoint("/ws")
            .addInterceptors(new StompWebSocketInterceptor())
            .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置消息代理
        // 以/app开头的消息会路由到@Controller中的@MessageMapping方法
        registry.setApplicationDestinationPrefixes("/app");

        // 启用简单消息代理，用于处理/topic和/queue开头的消息
        registry.enableSimpleBroker("/topic", "/queue");

        // 用户点对点发送消息的前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 配置客户端入站通道
        registration.interceptors(new StompWebSocketInterceptor());
    }
}