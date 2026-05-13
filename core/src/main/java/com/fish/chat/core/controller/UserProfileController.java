package com.fish.chat.core.controller;

import com.fish.chat.common.constants.UrlConstants;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.chat.SessionManager;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.req.UserUpdateRequest;
import com.fish.chat.core.repository.UserOnlineRepository;
import com.fish.chat.core.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户控制器
 */
@RestController
@RequestMapping(UrlConstants.HTTP_URL_PREFIX + "/user")
public class UserProfileController {

    @Resource
    private UserService userService;

    @Resource
    private SessionManager sessionManager;

    @Resource
    private UserOnlineRepository userOnlineRepository;

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
    public Result<PageResult<String>> getOnlineUsers(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(userOnlineRepository.getOnlineUserPage(pageNum, pageSize));
    }

    /**
     * 搜索用户（按用户名/昵称）
     */
    @GetMapping("/search")
    public Result<PageResult<UserDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<UserDTO> result = userService.searchUsers(keyword, pageNum, pageSize);
        if (result.getData() != null) {
            for (UserDTO u : result.getData()) {
                u.setOnline(sessionManager.isOnline(u.getCode()));
            }
        }
        return Result.success(result);
    }
}
