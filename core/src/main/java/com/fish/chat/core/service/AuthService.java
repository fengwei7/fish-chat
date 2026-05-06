package com.fish.chat.core.service;

import com.fish.chat.core.entity.dto.AuthDTO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.req.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 是否成功
     */
    boolean register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 认证响应（包含 token）
     */
    AuthDTO login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout();
}
