package com.fish.chat.service;

import com.fish.chat.entity.MongoChatMessage;
import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService {

    /**
     * 保存聊天消息
     *
     * @param chatMessage 聊天消息
     * @return 保存后的聊天消息
     */
    MongoChatMessage saveMessage(MongoChatMessage chatMessage);

    /**
     * 根据发送方和接收方查询聊天记录
     *
     * @param from 发送方用户ID
     * @param to   接收方用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findMessagesByFromAndTo(String from, String to);

    /**
     * 根据用户ID查询与该用户相关的所有聊天记录
     *
     * @param userId 用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findMessagesByUserId(String userId);

    /**
     * 根据发送方查询聊天记录
     *
     * @param from 发送方用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findMessagesByFrom(String from);

    /**
     * 根据接收方查询聊天记录
     *
     * @param to 接收方用户ID
     * @return 聊天记录列表
     */
    List<MongoChatMessage> findMessagesByTo(String to);
}