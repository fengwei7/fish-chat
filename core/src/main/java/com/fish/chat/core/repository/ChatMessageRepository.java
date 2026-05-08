package com.fish.chat.core.repository;

import com.fish.chat.core.entity.po.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB 聊天消息仓储
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 查询两用户私聊记录
     */
    List<ChatMessage> findByFromAndToAndRoomType(String from, String to, String roomType);

    /**
     * 查询房间消息历史（按时间倒序分页）
     */
    List<ChatMessage> findByRoomCodeOrderByTimestampDesc(String roomCode, Pageable pageable);

    /**
     * 查询用户在某个时间点之后收到的房间消息（用于同步）
     */
    List<ChatMessage> findByRoomCodeAndTimestampGreaterThanOrderByTimestampAsc(String roomCode, Long timestamp);

    /**
     * 查询用户发送的所有消息
     */
    List<ChatMessage> findByFromOrderByTimestampDesc(String from, Pageable pageable);

    /**
     * 按类型查询房间消息
     */
    List<ChatMessage> findByRoomCodeAndTypeOrderByTimestampDesc(String roomCode, String type, Pageable pageable);
}
