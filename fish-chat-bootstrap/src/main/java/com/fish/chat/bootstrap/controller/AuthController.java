package com.fish.chat.bootstrap.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.dto.AuthResponse;
import com.fish.chat.core.dto.LoginRequest;
import com.fish.chat.core.dto.RegisterRequest;
import com.fish.chat.core.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
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
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
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
