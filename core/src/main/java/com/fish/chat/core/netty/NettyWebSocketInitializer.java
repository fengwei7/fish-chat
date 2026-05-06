package com.fish.chat.core.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Netty WebSocket Channel 初始化器
 */
@Component
public class NettyWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private static final String WEBSOCKET_PATH = "/websocket/chat";

    @Resource
    private NettyWebSocketHandler nettyWebSocketHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // HTTP 编解码器
        pipeline.addLast("http-codec", new HttpServerCodec());

        // 支持大数据流
        pipeline.addLast("chunked-write", new ChunkedWriteHandler());

        // HTTP 消息聚合器
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(65536));

        // WebSocket 协议处理器
        pipeline.addLast("websocket-protocol", new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));

        // 自定义 WebSocket 消息处理器
        pipeline.addLast("websocket-handler", nettyWebSocketHandler);
    }
}
