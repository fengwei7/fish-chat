package com.fish.chat.controller.websocket;

import com.fish.chat.dto.UserDTO;
import com.fish.chat.service.ChatWebSocketService;
import com.fish.chat.utils.result.Result;

import com.fish.chat.websocket.handler.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket用户相关接口
 */
//@CrossOrigin
@RestController
@RequestMapping("/ws")
public class UserWSController {

    @Autowired
    ChatWebSocketService chatWebSocketService;

    /**
     * 获取在线用户数
     */
    @GetMapping("/online/count")
    public Result getOnlineCount() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", ChatWebSocketHandler.getOnlineCount());
        return Result.data(data);
    }

    /**
     * 发送消息给指定用户
     */
    @GetMapping("/send")
    public Result sendMessageToUser(@RequestParam String userId,
                                    @RequestParam String message) {
        chatWebSocketService.sendMessageToUser(userId, "notification", message);
        return Result.data("消息发送成功");
    }

    /**
     * 广播消息给所有在线用户
     */
    @GetMapping("/broadcast")
    public Result broadcastMessage(@RequestParam String message) {
        chatWebSocketService.broadcastMessage("notification", message);
        return Result.data("广播消息发送成功");
    }

    /**
     * 获取在线用户列表
     */
    @GetMapping("/online/users")
    public Result getOnlineUsers() {
        Map<String, Object> data = new HashMap<>();
        Map<String, UserDTO> onlineUsers = ChatWebSocketHandler.getOnlineUsers();
        data.put("users", onlineUsers);
        data.put("count", onlineUsers.size());
        return Result.data(data);
    }
}