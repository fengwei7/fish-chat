package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.req.UserUpdateRequest;
import com.fish.chat.core.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserProfileController {

    @Resource
    private UserService userService;

    /**
     * 查看当前用户信息
     */
    @GetMapping("/profile")
    public Result<UserDTO> getProfile() {
        UserDTO userDTO = userService.getCurrentUser();
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
     * 根据用户code查看用户信息
     */
    @GetMapping("/{code}")
    public Result<UserDTO> getUserByCode(@PathVariable String code) {
        UserDTO userDTO = userService.getUserByCode(code);
        return Result.success(userDTO);
    }
}
