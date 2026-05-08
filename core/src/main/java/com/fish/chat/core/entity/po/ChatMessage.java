package com.fish.chat.core.entity.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * MongoDB 聊天消息实体
 */
@Data
@Document(collection = "chat_messages")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** MongoDB 自动生成 _id */
    @Id
    private String id;

    /** 消息类型：TEXT / IMAGE / FILE / SYSTEM */
    @Field("type")
    @Indexed
    private String type;

    /** 发送方用户 code */
    @Field("from")
    @Indexed
    private String from;

    /** 接收方（私聊=对方code，群聊/频道=roomCode） */
    @Field("to")
    @Indexed
    private String to;

    /** 消息内容 */
    @Field("content")
    private String content;

    /** 时间戳（毫秒） */
    @Field("timestamp")
    @Indexed
    private Long timestamp;

    /** 房间ID */
    @Field("roomCode")
    @Indexed
    private String roomCode;

    /** 房间类型：PRIVATE / GROUP / CHANNEL */
    @Field("roomType")
    private String roomType;

    /** 文件名（图片/文件消息时） */
    @Field("fileName")
    private String fileName;

    /** 文件大小 */
    @Field("fileSize")
    private Long fileSize;
}
