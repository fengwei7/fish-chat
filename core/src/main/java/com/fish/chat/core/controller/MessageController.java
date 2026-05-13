package com.fish.chat.core.controller;

import com.fish.chat.common.constants.UrlConstants;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.ChatMessageDTO;
import com.fish.chat.core.entity.po.ChatMessage;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.repository.ChatMessageRepository;
import com.fish.chat.core.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息历史控制器
 */
@RestController
@RequestMapping(UrlConstants.HTTP_URL_PREFIX + "/messages")
public class MessageController {

    @Resource
    private ChatMessageRepository messageRepository;

    @Resource
    private UserRepository userRepository;

    /**
     * 拉取房间历史消息（分页）
     */
    @GetMapping("/{roomCode}")
    public Result<Map<String, Object>> getHistory(
            @PathVariable String roomCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ChatMessage> messages = messageRepository.findByRoomCodeOrderByTimestampDesc(
                roomCode, PageRequest.of(page, size));

        long total = messageRepository.countByRoomCode(roomCode);

        // 批量查询发送者信息，填充昵称和头像
        List<ChatMessageDTO> dtos = enrichWithUserInfo(messages);

        Map<String, Object> result = new HashMap<>();
        result.put("messages", dtos);
        result.put("page", page);
        result.put("size", size);
        result.put("total", total);
        return Result.success(result);
    }

    /**
     * 同步指定时间点之后的消息（断线重连用）
     */
    @GetMapping("/{roomCode}/sync")
    public Result<List<ChatMessageDTO>> syncMessages(
            @PathVariable String roomCode,
            @RequestParam long after) {

        List<ChatMessage> messages = messageRepository
                .findByRoomCodeAndTimestampGreaterThanOrderByTimestampAsc(roomCode, after);
        return Result.success(enrichWithUserInfo(messages));
    }

    /**
     * 批量查询发送者信息，填充昵称和头像到 DTO
     */
    private List<ChatMessageDTO> enrichWithUserInfo(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有发送者 code
        Set<String> senderCodes = messages.stream()
                .map(ChatMessage::getFrom)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询用户
        Map<String, UserPO> userMap = Collections.emptyMap();
        if (!senderCodes.isEmpty()) {
            List<UserPO> users = userRepository.selectByCodes(new ArrayList<>(senderCodes));
            userMap = users.stream().collect(Collectors.toMap(UserPO::getCode, u -> u));
        }

        // 组装 DTO
        List<ChatMessageDTO> dtos = new ArrayList<>(messages.size());
        for (ChatMessage msg : messages) {
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setId(msg.getId());
            dto.setType(msg.getType());
            dto.setFrom(msg.getFrom());
            dto.setTo(msg.getTo());
            dto.setContent(msg.getContent());
            dto.setTimestamp(msg.getTimestamp());
            dto.setRoomCode(msg.getRoomCode());
            dto.setRoomType(msg.getRoomType());
            dto.setFileName(msg.getFileName());
            dto.setFileSize(msg.getFileSize());

            // 优先使用 MongoDB 中已存储的昵称/头像，否则从用户表查询
            UserPO user = userMap.get(msg.getFrom());
            if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                dto.setSenderName(msg.getSenderName());
            } else if (user != null) {
                dto.setSenderName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            } else {
                dto.setSenderName(msg.getFrom());
            }

            if (msg.getSenderAvatar() != null && !msg.getSenderAvatar().isEmpty()) {
                dto.setSenderAvatar(msg.getSenderAvatar());
            } else if (user != null) {
                dto.setSenderAvatar(user.getAvatarUrl());
            }

            dtos.add(dto);
        }
        return dtos;
    }
}
