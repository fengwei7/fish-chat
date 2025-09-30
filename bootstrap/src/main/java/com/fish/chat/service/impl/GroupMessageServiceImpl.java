package com.fish.chat.service.impl;

import com.fish.chat.entity.MongoGroupMessage;
import com.fish.chat.mapper.mongo.MongoGroupMessageRepository;
import com.fish.chat.service.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMessageServiceImpl implements GroupMessageService {

    @Autowired
    private MongoGroupMessageRepository mongoGroupMessageRepository;

    @Override
    public MongoGroupMessage saveGroupMessage(MongoGroupMessage groupMessage) {
        return mongoGroupMessageRepository.save(groupMessage);
    }

    @Override
    public List<MongoGroupMessage> findGroupMessagesWithPagination(String groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return mongoGroupMessageRepository.findByGroupIdOrderByTimestampDesc(groupId, pageable).getContent();
    }

    @Override
    public List<MongoGroupMessage> findGroupMessagesByGroupId(String groupId) {
        return mongoGroupMessageRepository.findByGroupIdOrderByTimestamp(groupId);
    }
}