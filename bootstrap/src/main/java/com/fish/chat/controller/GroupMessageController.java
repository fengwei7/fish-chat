package com.fish.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.entity.MongoGroupMessage;
import com.fish.chat.service.GroupMessageService;
import com.fish.chat.utils.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group/message")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;

    /**
     * 分页获取群组消息
     *
     * @param groupId 群组ID
     * @param page 页码（默认1）
     * @param size 每页大小（默认20）
     * @return 群组消息列表
     */
    @GetMapping("/list/{groupId}")
    public Result getGroupMessages(@PathVariable String groupId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        // 限制每页最大数量
        if (size > 100) {
            size = 100;
        }

        List<MongoGroupMessage> messages = groupMessageService.findGroupMessagesWithPagination(
                groupId, page, size);

        return Result.data(messages);
    }

    /**
     * 获取指定群组的所有消息
     *
     * @param groupId 群组ID
     * @return 群组消息列表
     */
    @GetMapping("/list/all/{groupId}")
    public Result getAllGroupMessages(@PathVariable String groupId) {
        List<MongoGroupMessage> messages = groupMessageService.findGroupMessagesByGroupId(groupId);
        return Result.data(messages);
    }
}