package com.fish.chat.core.controller;

import com.fish.chat.common.constants.UrlConstants;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.AuthDTO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.req.RegisterRequest;
import com.fish.chat.core.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping(UrlConstants.HTTP_URL_PREFIX + "/auth")
public class AuthController {

    @Resource
    private AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Boolean> register(@Valid @RequestBody RegisterRequest request) {
        boolean success = authService.register(request);
        return success ? Result.success("注册成功", true) : Result.error("注册失败");
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<AuthDTO> login(@Valid @RequestBody LoginRequest request) {
        AuthDTO response = authService.login(request);
        return Result.success(response);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success("登出成功", null);
    }
}
