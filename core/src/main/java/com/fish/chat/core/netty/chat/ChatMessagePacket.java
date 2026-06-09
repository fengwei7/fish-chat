package com.fish.chat.core.netty.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * WebSocket 消息包 — 双端统一协议
 *
 * <pre>
 * 上行（客户端→服务端）：
 *   {"cmd":"MSG","reqId":"uuid","body":{"roomCode":"room_xxx","roomType":"GROUP","msgType":"TEXT","content":"hello"}}
 *
 * 下行（服务端→客户端）：
 *   {"cmd":"MSG","code":0,"reqId":"uuid","body":{"msgId":"xxx","senderId":"u1","roomCode":"room_xxx","content":"hello","timestamp":17000000}}
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePacket {

    /** 命令：MSG / ACK / ERROR / NOTIFY / HEARTBEAT */
    private String cmd;

    /** 状态码，0=成功 */
    private Integer code;

    /** 请求追踪ID（客户端生成，服务端回显） */
    private String reqCode;

    /** 消息体 */
    private Body body;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        /** 消息ID（MongoDB _id，服务端下发） */
        private String msgId;

        /** 发送者用户 code */
        private String senderCode;

        /** 发送者名称 */
        private String senderName;

        /** 发送者头像 */
        private String senderAvatar;

        /** 目标房间ID */
        private String roomCode;

        /** 房间类型：PRIVATE / GROUP / CHANNEL */
        private String roomType;

        /** 消息类型：TEXT / IMAGE / FILE / SYSTEM */
        private String msgType;

        /** 消息内容（TEXT 时为文本，IMAGE/FILE 时为文件URL） */
        private String content;

        /** 文件名（IMAGE / FILE 消息时） */
        private String fileName;

        /** 文件大小（字节） */
        private Long fileSize;

        /** 时间戳（毫秒） */
        private Long timestamp;

        /** 扩展字段 */
        private Map<String, Object> extra;
    }

    // ========== 工厂方法 ==========

    public static ChatMessagePacket msg(Body body) {
        return ChatMessagePacket.builder().cmd("MSG").code(0).body(body).build();
    }

    public static ChatMessagePacket ack(String reqCode) {
        return ChatMessagePacket.builder().cmd("ACK").code(0).reqCode(reqCode).build();
    }

    public static ChatMessagePacket error(String reqCode, String message) {
        return ChatMessagePacket.builder().cmd("ERROR").code(1).reqCode(reqCode)
                .body(Body.builder().content(message).build()).build();
    }

    public static ChatMessagePacket notify(String type, String content) {
        return ChatMessagePacket.builder().cmd("NOTIFY").code(0)
                .body(Body.builder().msgType(type).content(content).timestamp(System.currentTimeMillis()).build())
                .build();
    }

    public static ChatMessagePacket heartbeat() {
        return ChatMessagePacket.builder().cmd("HEARTBEAT").code(0).build();
    }
}
