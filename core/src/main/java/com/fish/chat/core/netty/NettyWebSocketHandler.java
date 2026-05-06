package com.fish.chat.core.netty;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.repository.ChatMessageRepository;
import com.fish.chat.core.repository.UserOnlineRepository;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty WebSocket 聊天处理器
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    // 在线会话管理: userId -> ChannelHandlerContext
    private static final Map<String, ChannelHandlerContext> onlineSessions = new ConcurrentHashMap<>();

    @Resource
    private UserOnlineRepository userOnlineRepository;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 处理 WebSocket 握手完成事件
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete =
                    (WebSocketServerProtocolHandler.HandshakeComplete) evt;

            // 从 URI 中获取 token
            String uri = handshakeComplete.requestUri();
            String token = extractToken(uri);

            if (token == null) {
                log.warn("WebSocket 连接缺少 token，关闭连接");
                ctx.close();
                return;
            }

            // 验证 token 并获取用户 ID
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId != null && !"undefined".equals(loginId.toString())) {
                    String userId = loginId.toString();

                    // 将 userId 保存到 Channel 属性中
                    ctx.channel().attr(NettyChannelAttributes.USER_ID).set(userId);

                    // 保存用户到 Redis
                    UserDTO userDTO = new UserDTO();
                    userOnlineRepository.saveOnlineUser(userId, userDTO, 5);

                    // 加入在线列表
                    onlineSessions.put(userId, ctx);

                    log.info("用户 {} 连接成功，当前在线人数：{}", userId, onlineSessions.size());

                    // 发送连接成功消息
                    sendMessage(ctx, buildMessage("connect", "连接成功"));
                } else {
                    log.warn("Token 无效，关闭连接");
                    ctx.close();
                }
            } catch (Exception e) {
                log.error("Token 验证失败: {}", e.getMessage());
                ctx.close();
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String userId = ctx.channel().attr(NettyChannelAttributes.USER_ID).get();

        if (userId != null) {
            // 移除在线状态
            onlineSessions.remove(userId);
            userOnlineRepository.removeOnlineUser(userId);

            log.info("用户 {} 断开连接，当前在线人数：{}", userId, onlineSessions.size());
        }

        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String userId = ctx.channel().attr(NettyChannelAttributes.USER_ID).get();

        if (userId == null) {
            log.warn("未认证的连接，关闭连接");
            ctx.close();
            return;
        }

        log.info("收到用户 {} 的消息：{}", userId, msg.text());

        try {
            // 解析消息
            Map<String, Object> messageMap = JSON.parseObject(msg.text(), Map.class);
            String type = (String) messageMap.get("type");

            switch (type) {
                case "chat":
                    handleChatMessage(userId, messageMap);
                    break;
                case "ping":
                    handlePingMessage(userId, ctx);
                    break;
                default:
                    sendMessage(ctx, buildMessage("error", "未知消息类型"));
            }
        } catch (Exception e) {
            log.error("处理消息失败：{}", e.getMessage(), e);
            sendMessage(ctx, buildMessage("error", "消息处理失败"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String userId = ctx.channel().attr(NettyChannelAttributes.USER_ID).get();
        log.error("用户 {} 连接发生异常：{}", userId, cause.getMessage(), cause);
        ctx.close();
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(String fromUserId, Map<String, Object> msg) throws Exception {
        String toUserId = (String) msg.get("to");
        String content = (String) msg.get("content");

        // 发送给目标用户
        ChannelHandlerContext toCtx = onlineSessions.get(toUserId);
        if (toCtx != null && toCtx.channel().isActive()) {
            String response = buildMessage("chat", content);
            sendMessage(toCtx, response);
        } else {
            // 用户不在线，返回错误信息给发送者
            ChannelHandlerContext fromCtx = onlineSessions.get(fromUserId);
            if (fromCtx != null && fromCtx.channel().isActive()) {
                sendMessage(fromCtx, buildMessage("error", "用户不在线"));
            }
        }

        // 保存到 MongoDB
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType("chat");
        chatMessage.setFrom(fromUserId);
        chatMessage.setTo(toUserId);
        chatMessage.setContent(content);
        chatMessage.setTimestamp(System.currentTimeMillis());
        chatMessageRepository.save(chatMessage);

        log.info("消息已保存: from={}, to={}, content={}", fromUserId, toUserId, content);
    }

    /**
     * 处理心跳消息
     */
    private void handlePingMessage(String userId, ChannelHandlerContext ctx) {
        // 更新 Redis 过期时间
        userOnlineRepository.updateOnlineUserExpire(userId, AuthConstants.ONLINE_USER_EXPIRE_MINUTES);

        try {
            sendMessage(ctx, buildMessage("pong", ""));
        } catch (Exception e) {
            log.error("发送心跳响应失败：{}", e.getMessage());
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(ChannelHandlerContext ctx, String message) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    /**
     * 构建消息 JSON
     */
    private String buildMessage(String type, String content) {
        Map<String, Object> msg = new ConcurrentHashMap<>();
        msg.put("type", type);
        msg.put("content", content);
        msg.put("timestamp", System.currentTimeMillis());
        return JSON.toJSONString(msg);
    }

    /**
     * 从 URI 中提取 token
     */
    private String extractToken(String uri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, List<String>> params = decoder.parameters();
        List<String> tokens = params.get(AuthConstants.WS_TOKEN_PARAM);

        if (tokens != null && !tokens.isEmpty()) {
            return tokens.get(0);
        }
        return null;
    }
}
