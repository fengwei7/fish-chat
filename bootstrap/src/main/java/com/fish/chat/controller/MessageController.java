package com.fish.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.entity.MongoGroupMessage;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.utils.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    /**
     * 分页获取用户之间的聊天记录
     * 
     * @param userId 对方用户ID
     * @param page 页码（默认1）
     * @param size 每页大小（默认20）
     * @return 聊天记录列表
     */
    @GetMapping("/user/{userId}")
    public Result getUserMessages(@PathVariable String userId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        String currentUserId = StpUtil.getLoginIdAsString();
        
        // 限制每页最大数量
        if (size > 100) {
            size = 100;
        }
        
        List<MongoChatMessage> messages = chatMessageService.findMessagesByFromAndToWithPagination(
                currentUserId, userId, page, size);
        
        return Result.data(messages);
    }
    
    /**
     * 分页获取群组消息
     * 
     * @param groupId 群组ID
     * @param page 页码（默认1）
     * @param size 每页大小（默认20）
     * @return 群组消息列表
     */
    @GetMapping("/group/{groupId}")
    public Result getGroupMessages(@PathVariable String groupId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        // 限制每页最大数量
        if (size > 100) {
            size = 100;
        }
        
        List<MongoGroupMessage> messages = chatMessageService.findGroupMessagesWithPagination(
                groupId, page, size);
        
        return Result.data(messages);
    }
    
    /**
     * 更新消息状态为已读
     * 
     * @param messageId 消息ID
     * @return 操作结果
     */
    @PostMapping("/read/{messageId}")
    public Result markAsRead(@PathVariable String messageId) {
        boolean success = chatMessageService.updateMessageStatus(messageId, "read");
        if (success) {
            return Result.ok("消息状态更新成功");
        } else {
            return Result.error("消息状态更新失败");
        }
    }
}