package com.fish.chat.core.service;

import com.fish.chat.core.entity.dto.AuthDTO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.req.RegisterRequest;

/**
 * 认证服务接口
 * 
 * 提供用户注册、登录、登出等认证相关功能
 * 
 * @author fengwei
 * @since 2026-06-09
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求，包含用户名、密码等信息
     * @return 注册是否成功
     * @throws com.fish.chat.common.exception.BusinessException 当用户名已存在或参数不合法时抛出
     */
    boolean register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求，包含用户名和密码
     * @return 认证响应，包含用户信息和访问令牌（token）
     * @throws com.fish.chat.common.exception.BusinessException 当用户名或密码错误时抛出
     */
    AuthDTO login(LoginRequest request);

    /**
     * 用户登出
     * 
     * 清除当前登录用户的会话信息
     * 
     * @throws cn.dev33.satoken.exception.NotLoginException 当用户未登录时抛出
     */
    void logout();
}
