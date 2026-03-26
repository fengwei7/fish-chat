package com.fish.chat.core.repository;

import com.fish.chat.core.entity.MongoChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * MongoDB 聊天消息仓储
 * 不需要@Repository 注解，Spring Data 会自动创建 bean
 */
public interface MongoChatMessageRepository extends MongoRepository<MongoChatMessage, String> {
    
    /**
     * 查询两个用户之间的聊天记录
     * @param fromUser 发送方 ID
     * @param toUser 接收方 ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findByFromAndTo(String fromUser, String toUser);
    
    /**
     * 查询用户发送的所有消息
     * @param fromUser 发送方 ID
     * @return 消息列表
     */
    List<MongoChatMessage> findByFrom(String fromUser);
    
    /**
     * 查询用户接收的所有消息
     * @param toUser 接收方 ID
     * @return 消息列表
     */
    List<MongoChatMessage> findByTo(String toUser);
    
    /**
     * 查询两个用户之间的聊天记录（分页）
     * @param fromUser 发送方 ID
     * @param toUser 接收方 ID
     * @param pageable 分页参数
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findByFromAndToOrderByTimestampDesc(String fromUser, String toUser, Pageable pageable);
}
