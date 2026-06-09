package com.fish.chat.core.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.common.constants.UrlConstants;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.ConversationDTO;
import com.fish.chat.core.service.ConversationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.fish.chat.common.constants.UrlConstants.HTTP_URL_PREFIX;

/**
 * 会话管理控制器
 */
@RestController
@RequestMapping(HTTP_URL_PREFIX + "/conversations")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    /**
     * 获取会话列表（按最后消息时间倒序）
     *
     * @param limit 返回数量限制，默认50
     * @return 会话列表
     */
    @GetMapping
    public Result<List<ConversationDTO>> listConversations(
            @RequestParam(defaultValue = "50") int limit) {
        String userCode = StpUtil.getLoginIdAsString();
        List<ConversationDTO> conversations = conversationService.listConversations(userCode, limit);
        return Result.success(conversations);
    }

    /**
     * 标记会话为已读（清除未读数）
     *
     * @param roomCode 房间编码
     * @return 操作结果
     */
    @PostMapping("/{roomCode}/read")
    public Result<Void> markAsRead(@PathVariable String roomCode) {
        String userCode = StpUtil.getLoginIdAsString();
        conversationService.clearUnread(userCode, roomCode);
        return Result.success("已标记为已读", null);
    }

    /**
     * 获取总未读消息数
     *
     * @return 总未读数
     */
    @GetMapping("/unread/total")
    public Result<Integer> getTotalUnreadCount() {
        String userCode = StpUtil.getLoginIdAsString();
        int totalUnread = conversationService.getTotalUnreadCount(userCode);
        return Result.success(totalUnread);
    }

    /**
     * 删除会话
     *
     * @param roomCode 房间编码
     * @return 操作结果
     */
    @DeleteMapping("/{roomCode}")
    public Result<Void> removeConversation(@PathVariable String roomCode) {
        String userCode = StpUtil.getLoginIdAsString();
        conversationService.removeConversation(userCode, roomCode);
        return Result.success("会话已删除", null);
    }
}
