package com.fish.chat.core.netty;

import com.fish.chat.core.netty.chat.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * Netty WebSocket 服务器
 */
@Slf4j
@Component
public class NettyWebSocketServer {

    @Value("${fish-chat-config.netty.websocket.port:8081}")
    private int port;

    @Resource
    private NettyWebSocketInitializer initializer;

    @Resource
    private SessionManager sessionManager;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * 启动 Netty WebSocket 服务器
     * 由 ApplicationStartupManager 统一调用
     */
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(initializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        ChannelFuture future = bootstrap.bind().sync();
        log.info("Netty WebSocket 启动成功！端口: {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭 Netty 服务器...");
            sessionManager.shutdown();
            shutdown();
        }));
    }

    @PreDestroy
    public void shutdown() {
        sessionManager.shutdown();
        if (bossGroup != null && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty 服务器已关闭");
    }
}
