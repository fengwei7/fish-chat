package com.fish.chat.interfaces.user.controller;

import com.fish.chat.application.user.dto.UserDTO;
import com.fish.chat.application.user.service.UserAppService;
import com.fish.chat.common.constants.UrlConstants;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.common.result.Result;
import com.fish.chat.interfaces.user.request.ChangePasswordRequest;
import com.fish.chat.interfaces.user.request.UserLoginRequest;
import com.fish.chat.interfaces.user.request.UserRegisterRequest;
import com.fish.chat.interfaces.user.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户接口控制器
 * 
 * 职责：
 * - 接收 HTTP 请求
 * - 参数校验
 * - 调用 AppService 处理业务
 * - 返回统一格式的响应
 * 
 * 设计原则：
 * - 薄 Controller：只负责接收请求和返回响应
 * - 不包含业务逻辑
 * - 使用 JSR-303 进行参数校验
 */
@RestController
@RequestMapping(UrlConstants.HTTP_URL_PREFIX + "/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserAppService userAppService;
    
    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 注册后的用户信息
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody UserRegisterRequest request) {
        UserDTO userDTO = userAppService.register(request.getUsername(), request.getPassword());
        return Result.success("注册成功", userDTO);
    }
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录结果（包含 token）
     */
    @PostMapping("/login")
    public Result<UserDTO> login(@Valid @RequestBody UserLoginRequest request) {
        UserDTO result = userAppService.login(
                request.getUsername(), 
                request.getPassword()
        );
        return Result.success(result);
    }
    
    /**
     * 获取当前用户信息
     * 
     * @param userCode 用户编码（从认证上下文获取）
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Result<UserDTO> getProfile(@RequestAttribute("userCode") String userCode) {
        UserDTO userDTO = userAppService.getCurrentUser(userCode);
        return Result.success(userDTO);
    }
    
    /**
     * 更新当前用户信息
     * 
     * @param userCode 用户编码（从认证上下文获取）
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public Result<UserDTO> updateProfile(
            @RequestAttribute("userCode") String userCode,
            @Valid @RequestBody UserUpdateRequest request) {
        UserDTO userDTO = userAppService.updateCurrentUser(
                userCode,
                request.getNickname(),
                request.getAvatarUrl(),
                request.getProfile(),
                request.getEmail(),
                request.getMobile()
        );
        return Result.success("更新成功", userDTO);
    }
    
    /**
     * 修改密码
     * 
     * @param userCode 用户编码（从认证上下文获取）
     * @param request 修改密码请求
     * @return 操作结果
     */
    @PostMapping("/password")
    public Result<Void> changePassword(
            @RequestAttribute("userCode") String userCode,
            @Valid @RequestBody ChangePasswordRequest request) {
        userAppService.changePassword(
                userCode, 
                request.getOldPassword(), 
                request.getNewPassword()
        );
        return Result.success("密码修改成功，请重新登录", null);
    }
    
    /**
     * 根据用户 Code 查看用户信息
     * 
     * @param code 用户编码
     * @return 用户信息
     */
    @GetMapping("/{code}")
    public Result<UserDTO> getUserByCode(@PathVariable String code) {
        UserDTO userDTO = userAppService.getUserByCode(code);
        return Result.success(userDTO);
    }
    
    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页的用户搜索结果
     */
    @GetMapping("/search")
    public Result<PageResult<UserDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<UserDTO> result = userAppService.searchUsers(keyword, pageNum, pageSize);
        return Result.success(result);
    }
}
