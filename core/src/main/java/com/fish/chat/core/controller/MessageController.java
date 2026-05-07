package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.repository.ChatMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息历史控制器
 */
@RestController
@RequestMapping("/messages")
@CrossOrigin
public class MessageController {

    @Resource
    private ChatMessageRepository messageRepository;

    /**
     * 拉取房间历史消息（分页）
     *
     * @param roomId 房间ID
     * @param page   页码（0-based）
     * @param size   每页数量（默认20）
     */
    @GetMapping("/{roomId}")
    public Result<Map<String, Object>> getHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ChatMessage> messages = messageRepository.findByRoomIdOrderByTimestampDesc(
                roomId, PageRequest.of(page, size));

        long total = messageRepository.count();
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        return Result.success(result);
    }

    /**
     * 同步指定时间点之后的消息（断线重连用）
     */
    @GetMapping("/{roomId}/sync")
    public Result<List<ChatMessage>> syncMessages(
            @PathVariable String roomId,
            @RequestParam long after) {

        List<ChatMessage> messages = messageRepository
                .findByRoomIdAndTimestampGreaterThanOrderByTimestampAsc(roomId, after);
        return Result.success(messages);
    }
}
