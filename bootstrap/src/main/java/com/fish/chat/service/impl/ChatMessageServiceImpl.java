package com.fish.chat.service.impl;

import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.MongoGroupMessage;
import com.fish.chat.mapper.mongo.MongoChatMessageRepository;
import com.fish.chat.mapper.mongo.MongoGroupMessageRepository;
import com.fish.chat.service.ChatMessageService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 聊天消息服务实现类
 */
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private MongoChatMessageRepository mongoChatMessageRepository;

    @Autowired
    private MongoGroupMessageRepository mongoGroupMessageRepository;

    @Override
    public MongoChatMessage saveMessage(MongoChatMessage chatMessage) {
        return mongoChatMessageRepository.save(chatMessage);
    }

    @Override
    public List<MongoChatMessage> findMessagesByFromAndTo(String from, String to) {
        return mongoChatMessageRepository.findByFromAndToOrderByTimestamp(from, to);
    }

    @Override
    public List<MongoChatMessage> findMessagesByUserId(String userId) {
        return mongoChatMessageRepository.findByUserId(userId);
    }

    @Override
    public List<MongoChatMessage> findMessagesByFrom(String from) {
        return mongoChatMessageRepository.findByFromOrderByTimestampDesc(from);
    }

    @Override
    public List<MongoChatMessage> findMessagesByTo(String to) {
        return mongoChatMessageRepository.findByToOrderByTimestampDesc(to);
    }

    @Override
    public List<MongoChatMessage> findMessagesByFromAndToWithPagination(String from, String to, int page, int size) {
        // 创建分页请求，按时间戳降序排列
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        // 查询正向消息（from -> to）
        Page<MongoChatMessage> forwardPage = mongoChatMessageRepository.findByFromAndToOrderByTimestampDesc(from, to,
            pageable);

        // 查询反向消息（to -> from）
        Page<MongoChatMessage> backwardPage = mongoChatMessageRepository.findByFromAndToOrderByTimestampDesc(to, from,
            pageable);

        // 合并结果
        List<MongoChatMessage> result = new ArrayList<>();
        result.addAll(forwardPage.getContent());
        result.addAll(backwardPage.getContent());

        // 按时间戳排序
        result.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));

        // 如果结果超过分页大小，截取前面的记录
        if (result.size() > size) {
            result = result.subList(0, size);
        }

        return result;
    }

    @Override
    public List<MongoGroupMessage> findGroupMessagesWithPagination(String groupId, int page, int size) {
        // 创建分页请求，按时间戳降序排列
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        // 查询群组消息
        Page<MongoGroupMessage> pageResult = mongoGroupMessageRepository.findByGroupIdOrderByTimestampDesc(groupId,
            pageable);

        return pageResult.getContent();
    }

    @Override
    public boolean updateMessageStatus(String messageId, String status) {
        MongoChatMessage message = mongoChatMessageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setStatus(status);
            mongoChatMessageRepository.save(message);
            return true;
        }
        return false;
    }

    @Override
    public MongoGroupMessage saveGroupMessage(MongoGroupMessage groupMessage) {
        return mongoGroupMessageRepository.save(groupMessage);
    }

    @Override
    public List<MongoGroupMessage> findGroupMessagesByGroupId(String groupId) {
        return mongoGroupMessageRepository.findByGroupIdOrderByTimestamp(groupId);
    }
}