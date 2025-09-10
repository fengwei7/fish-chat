package com.fish.chat.service.impl;

import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.mapper.mongo.MongoChatMessageRepository;
import com.fish.chat.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天消息服务实现类
 */
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private MongoChatMessageRepository mongoChatMessageRepository;

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
}