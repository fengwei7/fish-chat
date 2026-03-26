package com.fish.chat.core.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.dto.AuthResponse;
import com.fish.chat.core.dto.LoginRequest;
import com.fish.chat.core.dto.RegisterRequest;
import com.fish.chat.core.entity.User;
import com.fish.chat.core.mapper.mysql.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 用户注册
     * @param request 注册请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean register(RegisterRequest request) {
        // 检查用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 生成随机盐值
        String salt = RandomUtil.randomString(16);
        
        // 创建用户
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        // 使用 SHA256 加密并加盐
        newUser.setPassword(DigestUtil.sha256Hex(request.getPassword() + salt));
        newUser.setSalt(salt);
        newUser.setMobile(request.getMobile());
        newUser.setEmail(request.getEmail());
        newUser.setNickname(request.getNickname());
        newUser.setStatus(1); // 默认正常状态
        
        int rows = userMapper.insert(newUser);
        return rows > 0;
    }
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 认证响应
     */
    public AuthResponse login(LoginRequest request) {
        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 使用用户对应的盐值验证密码
        String inputPasswordHash = DigestUtil.sha256Hex(request.getPassword() + user.getSalt());
        if (!user.getPassword().equals(inputPasswordHash)) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账户已被禁用");
        }
        
        // 登录并生成 Token
        StpUtil.login(user.getId());
        
        // 构建响应
        return AuthResponse.builder()
                .token(StpUtil.getTokenValue())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }
}
