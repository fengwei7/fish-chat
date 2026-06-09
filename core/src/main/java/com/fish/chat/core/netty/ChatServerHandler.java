package com.fish.chat.core.netty;

import com.alibaba.fastjson2.JSON;
import com.fish.chat.core.chat.ChatMessagePacket;
import com.fish.chat.core.chat.ChatSession;
import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.chat.room.Room;
import com.fish.chat.core.chat.room.RoomManager;
import com.fish.chat.core.enums.RoomType;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.entity.po.GroupPO;
import com.fish.chat.core.entity.po.ChannelPO;
import com.fish.chat.core.enums.MessageType;
import com.fish.chat.core.repository.ChatMessageRepository;
import com.fish.chat.core.repository.GroupRepository;
import com.fish.chat.core.repository.ChannelRepository;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 核心聊天消息处理器
 *
 * 支持命令：
 *   MSG      — 发送消息（单聊/群聊/频道）
 *   HEARTBEAT — 心跳
 *   SYNC     — 同步错过的消息（断线重连后）
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ChatServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private SessionManager sessionManager;

    @Resource
    private RoomManager roomManager;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private ChannelRepository channelRepository;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String userCode = ctx.channel().attr(AuthHandshakeHandler.USER_CODE_KEY).get();

        if (userCode == null) {
            log.warn("未认证的消息，关闭连接");
            ctx.close();
            return;
        }

        // 刷新活跃时间
        ChatSession session = sessionManager.get(userCode);
        if (session != null) {
            session.touch();
        }

        String text = frame.text();
        log.debug("收到消息: userCode={} text={}", userCode, text);

        try {
            ChatMessagePacket packet = JSON.parseObject(text, ChatMessagePacket.class);
            if (packet.getCmd() == null) {
                sendError(ctx, null, "消息格式错误：缺少 cmd");
                return;
            }

            switch (packet.getCmd()) {
                case "MSG":
                    handleMessage(userCode, packet, ctx);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(userCode, ctx);
                    break;
                case "SYNC":
                    handleSync(userCode, packet, ctx);
                    break;
                default:
                    sendError(ctx, packet.getReqCode(), "未知命令: " + packet.getCmd());
            }
        } catch (Exception e) {
            log.error("消息处理异常: userCode={} msg={}", userCode, text, e);
            sendError(ctx, null, "消息解析失败");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 空闲超时 → 关闭连接
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                String userCode = ctx.channel().attr(AuthHandshakeHandler.USER_CODE_KEY).get();
                log.info("用户 {} 读超时，关闭连接", userCode);
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String userCode = ctx.channel().attr(AuthHandshakeHandler.USER_CODE_KEY).get();
        if (userCode != null) {
            sessionManager.unregister(userCode);
            log.info("用户 {} 断开连接", userCode);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String userCode = ctx.channel().attr(AuthHandshakeHandler.USER_CODE_KEY).get();
        log.error("连接异常: userCode={}", userCode, cause);
        ctx.close();
    }

    // ==================== 消息处理 ====================

    /**
     * 处理聊天消息（单聊/群聊/频道统一入口）
     */
    private void handleMessage(String userCode, ChatMessagePacket packet, ChannelHandlerContext ctx) {
        ChatMessagePacket.Body body = packet.getBody();
        if (body == null || body.getRoomCode() == null || body.getContent() == null) {
            sendError(ctx, packet.getReqCode(), "消息格式错误");
            return;
        }

        String roomCode = body.getRoomCode();
        String roomType = body.getRoomType();

        // 获取或创建房间
        Room room = resolveRoom(roomCode, roomType, userCode, body);
        if (room == null) {
            sendError(ctx, packet.getReqCode(), "房间不存在或无权访问");
            return;
        }

        // 检查频道发言权限
        if (room.getType() == RoomType.CHANNEL) {
            String channelCode = roomCode.substring(RoomType.CHANNEL.getValue().length() + 1);
            boolean isAdmin = channelRepository.isAdmin(channelCode, userCode);
            boolean isOwner = channelRepository.isOwner(channelCode, userCode);
            
            if (!isAdmin && !isOwner) {
                log.warn("用户 {} 无权在频道 {} 发言", userCode, roomCode);
                sendError(ctx, packet.getReqCode(), "仅频道管理员或创建者可以发言");
                return;
            }
        }

        // 设置消息属性
        body.setSenderCode(userCode);

        ChatSession session = sessionManager.get(userCode);
        body.setSenderName(session != null ? session.getUsername() : "");
        body.setSenderAvatar(session != null ? session.getAvatarUrl() : "");
        body.setTimestamp(System.currentTimeMillis());
        body.setRoomType(room.getType().name());

        // 持久化到 MongoDB
        String msgId = saveToMongo(body);
        body.setMsgId(msgId);

        // 构建下行包
        ChatMessagePacket out = ChatMessagePacket.msg(body);
        out.setReqCode(packet.getReqCode());

        // 广播到房间内所有在线成员
        Set<String> memberCodes = room.getMembers();
        sessionManager.broadcastToRoom(memberCodes, out, null); // 包括发送者自己（同步确认）

        // ACK
        sendAck(ctx, packet.getReqCode());
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(String userCode, ChannelHandlerContext ctx) {
        sessionManager.refreshOnlineStatus(userCode);
        sendPacket(ctx, ChatMessagePacket.heartbeat());
    }

    /**
     * 处理消息同步（断线重连后拉取未收消息）
     * body.extra.lastTimestamp — 最后收到的消息时间戳
     */
    private void handleSync(String userCode, ChatMessagePacket packet, ChannelHandlerContext ctx) {
        ChatMessagePacket.Body body = packet.getBody();
        Long lastTimestamp = null;

        if (body != null && body.getExtra() != null) {
            Object ts = body.getExtra().get("lastTimestamp");
            if (ts instanceof Number) {
                lastTimestamp = ((Number) ts).longValue();
            }
        }

        // 获取用户所在的所有房间
        ChatSession session = sessionManager.get(userCode);
        if (session != null) {
            for (String roomCode : session.getJoinedRooms()) {
                Room room = roomManager.getRoom(roomCode);
                if (room != null && room.isMember(userCode)) {
                    // 查询每个房间的最新消息（一次发一条给用户提示）
                    // 实际中应该批量推送，这里简化处理
                    ChatMessagePacket notify = ChatMessagePacket.notify("SYNC-READY",
                            "房间 " + roomCode + " 同步就绪，请拉取历史消息");
                    sendPacket(ctx, notify);
                }
            }
        }

        sendAck(ctx, packet.getReqCode());
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析/创建房间
     */
    private Room resolveRoom(String roomCode, String roomType, String userCode, ChatMessagePacket.Body body) {
        // 私聊：roomCode 应该由客户端用 Room.buildPrivateRoomCode() 生成
        // 或者传两个用户 code 让服务端生成
        if (RoomType.PRIVATE.getValue().equalsIgnoreCase(roomType)) {
            // 解析 roomCode 获取两个用户
            String[] parts = roomCode.split(":");
            if (parts.length == 3 && RoomType.PRIVATE.getValue().equals(parts[0])) {
                String user1 = parts[1];
                String user2 = parts[2];
                Room room = roomManager.getOrCreatePrivateRoom(user1, user2);
                if (!room.isMember(userCode)) {
                    room.addMember(userCode);
                }
                return room;
            }
            // fallback: 从 body.extra 取 targetId 来创建
            if (body.getExtra() != null && body.getExtra().get("targetId") != null) {
                String targetId = body.getExtra().get("targetId").toString();
                Room room = roomManager.getOrCreatePrivateRoom(userCode, targetId);
                return room;
            }
            return null;
        }

        // 群聊/频道：直接取已有房间
        Room room = roomManager.getRoom(roomCode);
        if (room == null) {
            // 尝试从数据库加载
            if (roomCode.startsWith(RoomType.GROUP.getValue() + ":")) {
                String groupCode = roomCode.substring(RoomType.GROUP.getValue().length() + 1);
                GroupPO group = groupRepository.selectByCode(groupCode);
                if (group != null) {
                    // 检查用户是否是群成员
                    if (!groupRepository.isMember(groupCode, userCode)) {
                        log.warn("用户 {} 不是群组 {} 的成员", userCode, groupCode);
                        return null;
                    }
                    // 创建并缓存房间
                    room = roomManager.getOrCreateGroupRoom(groupCode, group.getName(), group.getAvatar(), null);
                    room.addMember(userCode);
                }
            } else if (roomCode.startsWith(RoomType.CHANNEL.getValue() + ":")) {
                String channelCode = roomCode.substring(RoomType.CHANNEL.getValue().length() + 1);
                ChannelPO channel = channelRepository.selectByCode(channelCode);
                if (channel != null) {
                    // 检查用户是否有权限访问频道
                    if (!channelRepository.isMember(channelCode, userCode)) {
                        log.warn("用户 {} 不是频道 {} 的订阅者", userCode, channelCode);
                        return null;
                    }
                    // 创建并缓存房间
                    room = roomManager.getOrCreateChannelRoom(channelCode, channel.getName(), channel.getAvatar(), null);
                    room.addMember(userCode);
                }
            }
            
            if (room == null) {
                log.warn("房间不存在: {}", roomCode);
                return null;
            }
        }

        if (!room.isMember(userCode)) {
            log.warn("用户 {} 不在房间 {} 中", userCode, roomCode);
            return null;
        }

        return room;
    }

    /**
     * 保存消息到 MongoDB
     */
    private String saveToMongo(ChatMessagePacket.Body body) {
        ChatMessage msg = new ChatMessage();
        msg.setType(body.getMsgType() != null ? body.getMsgType() : MessageType.TEXT.getValue());
        msg.setFrom(body.getSenderCode());
        msg.setTo(body.getRoomCode());
        msg.setContent(body.getContent());
        msg.setTimestamp(body.getTimestamp() != null ? body.getTimestamp() : System.currentTimeMillis());
        msg.setRoomCode(body.getRoomCode());
        msg.setRoomType(body.getRoomType());
        msg.setSenderName(body.getSenderName());
        msg.setSenderAvatar(body.getSenderAvatar());
        msg.setFileName(body.getFileName());
        msg.setFileSize(body.getFileSize());

        ChatMessage saved = chatMessageRepository.save(msg);
        return saved.getId();
    }

    private void sendAck(ChannelHandlerContext ctx, String reqId) {
        sendPacket(ctx, ChatMessagePacket.ack(reqId));
    }

    private void sendError(ChannelHandlerContext ctx, String reqId, String message) {
        sendPacket(ctx, ChatMessagePacket.error(reqId, message));
    }

    private void sendPacket(ChannelHandlerContext ctx, ChatMessagePacket packet) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(packet)));
        }
    }
}
