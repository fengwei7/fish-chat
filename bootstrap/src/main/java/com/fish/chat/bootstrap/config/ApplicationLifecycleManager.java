package com.fish.chat.bootstrap.config;

import com.fish.chat.core.netty.chat.SessionManager;
import com.fish.chat.core.netty.MessageBatchWriter;
import com.fish.chat.core.netty.NettyWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 应用生命周期管理器
 * 
 * 统一管理所有组件的启动和关闭逻辑，避免分散在各个类中使用 @PostConstruct 和 @PreDestroy
 * 这样便于：
 * 1. 统一控制初始化和销毁顺序
 * 2. 集中管理启动和关闭日志
 * 3. 便于后续添加健康检查
 * 4. 支持按需初始化和延迟初始化
 */
@Slf4j
@Component
public class ApplicationLifecycleManager {

    @Resource
    private SessionManager sessionManager;
    
    @Resource
    private MessageBatchWriter messageBatchWriter;
    
    @Resource
    private NettyWebSocketServer nettyWebSocketServer;

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
        logBorder("开始初始化 Fish-Chat 应用组件...");
        
        try {
            log.info("初始化 SessionManager...");
            sessionManager.init();
            
            log.info("启动 MessageBatchWriter...");
            messageBatchWriter.startBatchWriter();
            
            log.info("启动 NettyWebSocketServer...");
            nettyWebSocketServer.start();
            
            logBorder("Fish-Chat 应用组件初始化完成！");
        } catch (Exception e) {
            log.error("应用组件初始化失败！", e);
            throw new RuntimeException("应用启动失败", e);
        }
    }
    
    /**
     * 应用关闭时统一销毁所有组件
     * 
     * 销毁顺序与初始化相反：
     * 1. 先关闭网络服务（NettyWebSocketServer）
     * 2. 再关闭业务组件（MessageBatchWriter - 保存剩余消息）
     * 3. 最后关闭基础组件（SessionManager）
     */
    @PreDestroy
    public void destroyAll() {
        logBorder("开始关闭 Fish-Chat 应用组件...");
        
        try {
            log.info("关闭 NettyWebSocketServer...");
            nettyWebSocketServer.shutdown();
            
            log.info("关闭 MessageBatchWriter...");
            messageBatchWriter.shutdown();
            
            log.info("关闭 SessionManager...");
            sessionManager.shutdown();
            
            logBorder("Fish-Chat 应用组件关闭完成！");
        } catch (Exception e) {
            log.error("应用组件关闭失败！", e);
            // 关闭阶段不抛出异常，避免影响 JVM 关闭
        }
    }
    
    /**
     * 打印分隔线日志
     */
    private void logBorder(String message) {
        log.info("========================================");
        log.info(message);
        log.info("========================================");
    }
}
