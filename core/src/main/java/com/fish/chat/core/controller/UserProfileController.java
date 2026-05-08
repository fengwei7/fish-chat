package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.req.UserUpdateRequest;
import com.fish.chat.core.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserProfileController {

    @Resource
    private UserService userService;

    @Resource
    private SessionManager sessionManager;

    /**
     * 查看当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        UserDTO userDTO = userService.getCurrentUser();
        userDTO.setOnline(sessionManager.isOnline(userDTO.getCode()));
        return Result.success(userDTO);
    }

    /**
     * 修改当前用户信息
     */
    @PostMapping("/profile")
    public Result<UserDTO> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        UserDTO userDTO = userService.updateCurrentUser(request);
        return Result.success("更新成功", userDTO);
    }

    /**
     * 根据用户 code 查看用户信息
     */
    @GetMapping("/{code}")
    public Result<UserDTO> getUserByCode(@PathVariable String code) {
        UserDTO userDTO = userService.getUserByCode(code);
        userDTO.setOnline(sessionManager.isOnline(userDTO.getCode()));
        return Result.success(userDTO);
    }

    /**
     * 获取在线用户列表
     */
    @GetMapping("/online")
    public Result<Map<String, Object>> getOnlineUsers() {
        Set<String> onlineIds = sessionManager.getAllOnlineUserCodes();
        Map<String, Object> result = new HashMap<>();
        result.put("count", onlineIds.size());
        result.put("userCodes", new ArrayList<>(onlineIds));
        return Result.success(result);
    }

    /**
     * 搜索用户（按用户名/昵称）
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> search(@RequestParam String keyword) {
        List<UserDTO> users = userService.searchUsers(keyword);
        for (UserDTO u : users) {
            u.setOnline(sessionManager.isOnline(u.getCode()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", users.size());
        result.put("users", users);
        return Result.success(result);
    }
}
