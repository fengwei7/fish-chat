package com.fish.chat.entity;

import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "group_messages")
public class MongoGroupMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    private String id;

    /**
     * 消息类型
     */
    @Field("type")
    private String type;

    /**
     * 发送方用户ID
     */
    @Field("from")
    @Indexed
    private String from;

    /**
     * 群组ID
     */
    @Field("group_id")
    @Indexed
    private String groupId;

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

    /**
     * 消息状态 (sent: 已发送, read: 已读)
     */
    @Field("status")
    private String status = "sent";
}