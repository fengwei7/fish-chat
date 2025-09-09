package com.fish.chat.controller.websocket;

import com.fish.chat.service.UserService;
import com.fish.chat.utils.result.Result;
import com.fish.chat.utils.websocket.WebSocketUtil;
import com.fish.chat.websocket.ChatWebSocket;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket测试控制器
 */
@RestController
@RequestMapping("/ws")
public class UserWSController {
    /**
     * 获取在线用户数
     */
    @GetMapping("/online/count")
    public Result getOnlineCount() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", ChatWebSocket.getOnlineCount());
        return Result.data(data);
    }

    /**
     * 发送消息给指定用户
     */
    @GetMapping("/send")
    public Result sendMessageToUser(@RequestParam String userId,
        @RequestParam String message) {
        WebSocketUtil.sendMessageToUser(userId, "notification", message);
        return Result.data("消息发送成功");
    }

    /**
     * 广播消息给所有在线用户
     */
    @GetMapping("/broadcast")
    public Result broadcastMessage(@RequestParam String message) {
        WebSocketUtil.broadcastMessage("notification", message);
        return Result.data("广播消息发送成功");
    }

    /**
     * 获取在线用户列表
     */
    @GetMapping("/online/users")
    public Result getOnlineUsers() {
        Map<String, Object> data = new HashMap<>();
        data.put("users", ChatWebSocket.getOnlineClients().keySet());
        data.put("count", ChatWebSocket.getOnlineCount());
        return Result.data(data);
    }
}