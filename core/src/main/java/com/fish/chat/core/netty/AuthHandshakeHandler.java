package com.fish.chat.core.netty;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.common.constants.AuthConstants;
import com.fish.chat.core.netty.chat.SessionManager;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.repository.UserRepository;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手认证处理器
 *
 * 在 WebSocket 协议升级后、首条消息到达前验证 token
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class AuthHandshakeHandler extends ChannelInboundHandlerAdapter {

    public static final AttributeKey<String> USER_CODE_KEY = AttributeKey.valueOf("userCode");
    public static final AttributeKey<String> USERNAME_KEY = AttributeKey.valueOf("username");
    public static final AttributeKey<String> AVATAR_KEY = AttributeKey.valueOf("avatarUrl");

    @Resource
    private SessionManager sessionManager;

    @Resource
    private UserRepository userRepository;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete =
                    (WebSocketServerProtocolHandler.HandshakeComplete) evt;

            String uri = complete.requestUri();
            String token = extractToken(uri);

            if (token == null) {
                log.warn("WebSocket 握手失败：缺少 token");
                ctx.close();
                return;
            }

            try {
                // 用 Sa-Token 验证
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null || "undefined".equals(loginId.toString())) {
                    log.warn("WebSocket 握手失败：token 无效");
                    ctx.close();
                    return;
                }

                String loginIdStr = loginId.toString();
                // loginId 可能是用户ID（Long）或 code
                UserPO user = resolveUser(loginIdStr);
                if (user == null) {
                    log.warn("WebSocket 握手失败：用户不存在 {}", loginIdStr);
                    ctx.close();
                    return;
                }

                // 保存用户信息到 Channel 属性
                ctx.channel().attr(USER_CODE_KEY).set(user.getCode());
                ctx.channel().attr(USERNAME_KEY).set(user.getNickname() != null ? user.getNickname() : user.getUsername());
                ctx.channel().attr(AVATAR_KEY).set(user.getAvatarUrl() != null ? user.getAvatarUrl() : "");

                // 注册到 SessionManager
                sessionManager.register(
                        user.getCode(),
                        user.getNickname() != null ? user.getNickname() : user.getUsername(),
                        user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                        ctx.channel()
                );

                log.info("WebSocket 认证成功：user={} code={}", user.getUsername(), user.getCode());
            } catch (Exception e) {
                log.error("WebSocket 认证异常: {}", e.getMessage());
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 从 URI 查询参数中提取 token
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

    /**
     * 解析用户 — 支持按ID（数字）或code（字符串）查询
     */
    private UserPO resolveUser(String loginId) {
        // 先按 code 查
        UserPO user = userRepository.selectByCode(loginId);
        if (user != null) return user;

        // 再按 id 查
        try {
            user = userRepository.selectById(loginId);
        } catch (Exception ignored) {
        }
        return user;
    }
}
