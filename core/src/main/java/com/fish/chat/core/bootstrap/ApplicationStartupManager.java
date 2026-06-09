package com.fish.chat.core.bootstrap;

import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.netty.MessageBatchWriter;
import com.fish.chat.core.netty.NettyWebSocketServer;
import com.fish.chat.core.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 应用启动管理器
 * 
 * 统一管理所有组件的初始化逻辑，避免分散在各个类中使用 @PostConstruct
 * 这样便于：
 * 1. 统一控制初始化顺序
 * 2. 集中管理启动日志
 * 3. 便于后续添加启动健康检查
 * 4. 支持按需初始化和延迟初始化
 */
@Slf4j
@Component
public class ApplicationStartupManager {

    @Resource
    private SessionManager sessionManager;
    
    @Resource
    private MessageBatchWriter messageBatchWriter;
    
    @Resource
    private NettyWebSocketServer nettyWebSocketServer;
    
    @Resource
    private ConversationService conversationService;

    /**
     * 应用启动时统一初始化所有组件
     * 
     * 初始化顺序很重要：
     * 1. 先初始化基础组件（SessionManager）
     * 2. 再初始化业务组件（MessageBatchWriter）
     * 3. 最后启动网络服务（NettyWebSocketServer）
     */
    @PostConstruct
    public void initializeAll() {
        log.info("========================================");
        log.info("开始初始化 Fish-Chat 应用组件...");
        log.info("========================================");
        
        try {
            // 1. 初始化会话管理器（清理Redis脏数据）
            log.info("[1/4] 初始化 SessionManager...");
            sessionManager.init();
            
            // 2. 启动消息批量写入器
            log.info("[2/4] 启动 MessageBatchWriter...");
            messageBatchWriter.startBatchWriter();
            
            // 3. 启动 Netty WebSocket 服务器
            log.info("[3/4] 启动 NettyWebSocketServer...");
            nettyWebSocketServer.start();
            
            log.info("========================================");
            log.info("Fish-Chat 应用组件初始化完成！");
            log.info("========================================");
        } catch (Exception e) {
            log.error("应用组件初始化失败！", e);
            throw new RuntimeException("应用启动失败", e);
        }
    }
}
