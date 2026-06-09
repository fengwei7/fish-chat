package com.fish.chat.core.netty;

import com.fish.chat.core.chat.ChatMessagePacket;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.enums.MessageType;
import com.fish.chat.core.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 消息批量写入器 - 异步持久化消息到 MongoDB
 * 
 * 使用 BlockingQueue 缓冲消息，定时或达到批量大小时批量写入数据库
 * 提高消息吞吐量，避免同步写入影响性能
 */
@Slf4j
@Component
public class MessageBatchWriter {

    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    @Value("${fish-chat-config.mongo-message-batch.queue-capacity:10000}")
    private int queueCapacity;

    @Value("${fish-chat-config.mongo-message-batch.batch-size:100}")
    private int batchSize;

    @Value("${fish-chat-config.mongo-message-batch.poll-timeout-ms:100}")
    private long pollTimeoutMs;

    /** 消息缓冲队列 */
    private BlockingQueue<ChatMessagePacket.Body> queue;

    @PostConstruct
    public void startBatchWriter() {
        // 初始化队列
        queue = new LinkedBlockingQueue<>(queueCapacity);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChatMessagePacket.Body> batch = new ArrayList<>();
            
            log.info("消息批量写入器已启动，队列容量: {}, 批量大小: {}, 轮询超时: {}ms", 
                    queueCapacity, batchSize, pollTimeoutMs);
            
            while (true) {
                try {
                    ChatMessagePacket.Body body = queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
                    if (body != null) {
                        batch.add(body);
                    }

                    // 当批次达到指定大小或队列为空但有待处理数据时，执行批量保存
                    if (batch.size() >= batchSize || (body == null && !batch.isEmpty())) {
                        batchSave(batch);
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    log.warn("消息批量写入器被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("消息批量写入异常", e);
                }
            }
        });
    }

    /**
     * 添加消息到批量写入队列
     * 在添加前先生成消息ID
     * 
     * @param body 消息体
     * @return 生成的消息ID
     */
    public String addMessage(ChatMessagePacket.Body body) {
        // 生成消息ID
        String msgId = UUID.randomUUID().toString().replace("-", "");
        body.setMsgId(msgId);
        
        boolean offered = queue.offer(body);
        if (!offered) {
            log.warn("消息队列已满，丢弃消息: roomCode={}, senderCode={}", 
                    body.getRoomCode(), body.getSenderCode());
        }
        return msgId;
    }

    /**
     * 批量保存消息到 MongoDB
     * 
     * @param batch 消息批次
     */
    private void batchSave(List<ChatMessagePacket.Body> batch) {
        if (batch.isEmpty()) {
            return;
        }

        try {
            List<ChatMessage> messages = new ArrayList<>(batch.size());
            for (ChatMessagePacket.Body body : batch) {
                ChatMessage msg = convertToEntity(body);
                messages.add(msg);
            }

            // 使用 MongoTemplate 批量插入，保留已生成的ID
            mongoTemplate.insert(messages, ChatMessage.class);

            log.debug("批量保存消息成功，数量: {}", messages.size());
        } catch (Exception e) {
            log.error("批量保存消息失败，批次大小: {}", batch.size(), e);
            // 这里可以考虑将失败的消息重新入队或记录到磁盘
            // 目前仅记录日志，避免阻塞主流程
        }
    }

    /**
     * 将消息体转换为 MongoDB 实体
     * 
     * @param body 消息体
     * @return ChatMessage 实体
     */
    private ChatMessage convertToEntity(ChatMessagePacket.Body body) {
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
        return msg;
    }

    /**
     * 获取队列中待处理的消息数量
     * 
     * @return 待处理消息数
     */
    public int getPendingCount() {
        return queue.size();
    }
}
