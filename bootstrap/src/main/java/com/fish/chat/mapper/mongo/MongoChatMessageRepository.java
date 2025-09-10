package com.fish.chat.mapper.mongo;

import com.fish.chat.entity.MongoChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天消息MongoDB操作接口
 */
@Repository
public interface MongoChatMessageRepository extends MongoRepository<MongoChatMessage, String> {

    /**
     * 根据发送方和接收方查询聊天记录
     *
     * @param from 发送方用户ID
     * @param to   接收方用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findByFromAndToOrderByTimestamp(String from, String to);

    /**
     * 根据用户ID查询与该用户相关的所有聊天记录
     *
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    @Query("{$or: [{from: ?0}, {to: ?0}]}")
    List<MongoChatMessage> findByUserId(String userId);

    /**
     * 根据发送方查询聊天记录
     *
     * @param from 发送方用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findByFromOrderByTimestampDesc(String from);

    /**
     * 根据接收方查询聊天记录
     *
     * @param to 接收方用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findByToOrderByTimestampDesc(String to);
}