package com.fish.chat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.entity.MongoChatMessage;
import com.fish.chat.service.ChatMessageService;
import com.fish.chat.utils.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 */
@CrossOrigin
@RestController
@RequestMapping("/chat/messages")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 查询与指定用户的聊天记录
     *
     * @param userId 对方用户ID
     * @return 聊天记录列表
     */
    @GetMapping("/user/{userId}")
    public Result getMessagesWithUser(@PathVariable String userId) {
        String currentUserId = StpUtil.getLoginIdAsString();
        List<MongoChatMessage> messages = chatMessageService.findMessagesByFromAndTo(currentUserId, userId);
        // 如果没有找到正向的，则查找反向的
        if (messages.isEmpty()) {
            messages = chatMessageService.findMessagesByFromAndTo(userId, currentUserId);
        }
        return Result.data(messages);
    }

    /**
     * 查询当前用户发送的所有消息
     *
     * @return 聊天记录列表
     */
    @GetMapping("/sent")
    public Result getSentMessages() {
        String currentUserId = StpUtil.getLoginIdAsString();
        List<MongoChatMessage> messages = chatMessageService.findMessagesByFrom(currentUserId);
        return Result.data(messages);
    }

    /**
     * 查询当前用户接收的所有消息
     *
     * @return 聊天记录列表
     */
    @GetMapping("/received")
    public Result getReceivedMessages() {
        String currentUserId = StpUtil.getLoginIdAsString();
        List<MongoChatMessage> messages = chatMessageService.findMessagesByTo(currentUserId);
        return Result.data(messages);
    }

    /**
     * 查询与当前用户相关的所有消息（包括发送和接收）
     *
     * @return 聊天记录列表
     */
    @GetMapping("/all")
    public Result getAllMessages() {
        String currentUserId = StpUtil.getLoginIdAsString();
        List<MongoChatMessage> messages = chatMessageService.findMessagesByUserId(currentUserId);
        return Result.data(messages);
    }
}