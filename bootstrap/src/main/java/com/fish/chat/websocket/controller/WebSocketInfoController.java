package com.fish.chat.websocket.controller;

import com.fish.chat.websocket.service.GroupCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketInfoController {

    @Autowired
    private SimpUserRegistry userRegistry;
    
    @Autowired
    private GroupCacheService groupCacheService;

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    @GetMapping("/online/count")
    public Map<String, Object> getOnlineCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", userRegistry.getUserCount());
        result.put("message", "success");
        return result;
    }

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    @GetMapping("/online/users")
    public Map<String, Object> getOnlineUsers() {
        Map<String, Object> result = new HashMap<>();
        Collection<String> users = userRegistry.getUsers().stream()
                .map(user -> user.getName())
                .collect(java.util.stream.Collectors.toList());
        result.put("code", 200);
        result.put("data", users);
        result.put("message", "success");
        return result;
    }
    
    /**
     * 更新群组成员缓存
     *
     * @param groupId 群组ID
     * @return 操作结果
     */
    @PostMapping("/group/{groupId}/cache/update")
    public Map<String, Object> updateGroupCache(@PathVariable String groupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            groupCacheService.updateGroupMembersCache(groupId);
            result.put("code", 200);
            result.put("message", "群组成员缓存更新成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "群组成员缓存更新失败: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 清除群组缓存
     *
     * @param groupId 群组ID
     * @return 操作结果
     */
    @DeleteMapping("/group/{groupId}/cache")
    public Map<String, Object> clearGroupCache(@PathVariable String groupId) {
        Map<String, Object> result = new HashMap<>();
        try {
            groupCacheService.removeGroupFromCache(groupId);
            result.put("code", 200);
            result.put("message", "群组成员缓存清除成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "群组成员缓存清除失败: " + e.getMessage());
        }
        return result;
    }
}