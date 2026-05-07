package com.fish.chat.core.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Netty WebSocket Channel 初始化器
 */
@Component
public class NettyWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/ws";

    @Resource
    private AuthHandshakeHandler authHandshakeHandler;

    @Resource
    private ChatServerHandler chatServerHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // 1. HTTP 编解码
        pipeline.addLast("http-codec", new HttpServerCodec());
        // 2. 大数据流支持
        pipeline.addLast("chunked-write", new ChunkedWriteHandler());
        // 3. HTTP 消息聚合（最大 64KB）
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));
        // 4. WebSocket 协议升级（路径 /ws，最大帧 64KB）
        pipeline.addLast("websocket-protocol",
                new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, 65536));
        // 5. Token 认证（握手后验证）
        pipeline.addLast("auth-handler", authHandshakeHandler);
        // 6. 空闲检测：180秒无读关闭连接
        pipeline.addLast("idle-handler", new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));
        // 7. 核心消息处理器
        pipeline.addLast("chat-handler", chatServerHandler);
    }
}
