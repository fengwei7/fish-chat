package com.fish.chat.core.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * MongoDB 聊天消息实体类
 */
@Data
@Document(collection = "chat_messages")
public class MongoChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @Id
    private String id;

    /**
     * 消息类型
     */
    @Field("type")
    private String type;

    /**
     * 发送方用户 ID
     */
    @Field("from")
    @Indexed
    private String from;

    /**
     * 接收方用户 ID
     */
    @Field("to")
    @Indexed
    private String to;

    /**
     * 消息内容
     */
    @Field("content")
    private String content;

    /**
     * 时间戳
     */
    @Field("timestamp")
    @Indexed
    private Long timestamp;
}
