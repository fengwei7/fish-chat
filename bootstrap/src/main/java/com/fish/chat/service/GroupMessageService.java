package com.fish.chat.service;

import com.fish.chat.entity.MongoGroupMessage;
import java.util.List;

public interface GroupMessageService {

    /**
     * 保存群组消息
     *
     * @param groupMessage 群组消息
     * @return 保存后的群组消息
     */
    MongoGroupMessage saveGroupMessage(MongoGroupMessage groupMessage);

    /**
     * 分页查询群组消息
     *
     * @param groupId 群组ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 群组消息列表
     */
    List<MongoGroupMessage> findGroupMessagesWithPagination(String groupId, int page, int size);

    /**
     * 根据群组ID查询群组消息
     *
     * @param groupId 群组ID
     * @return 群组消息列表
     */
    List<MongoGroupMessage> findGroupMessagesByGroupId(String groupId);
}