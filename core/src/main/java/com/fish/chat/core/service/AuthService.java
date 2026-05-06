package com.fish.chat.core.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.entity.dto.AuthDTO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.req.RegisterRequest;
import com.fish.chat.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    @Resource
    private UserRepository userRepository;
    
    /**
     * 用户注册
     * @param request 注册请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean register(RegisterRequest request) {
        // 检查用户名是否存在
        UserPO existingUserPO = userRepository.findByUsername(request.getUsername());
        
        if (existingUserPO != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 生成随机盐值
        String salt = RandomUtil.randomString(16);
        
        // 创建用户
        UserPO userPO = new UserPO();
        userPO.setUsername(request.getUsername());
        // 使用 SHA256 加密并加盐
        userPO.setPassword(DigestUtil.sha256Hex(request.getPassword() + salt));
        userPO.setSalt(salt);
        userPO.setMobile(request.getMobile());
        userPO.setEmail(request.getEmail());
        userPO.setNickname(request.getNickname());
        userPO.setStatus(1); // 默认正常状态
        
        userRepository.save(userPO);
        return true;
    }
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 认证响应
     */
    public AuthDTO login(LoginRequest request) {
        // 查询用户
        UserPO userPO = userRepository.findByUsername(request.getUsername());
        
        if (userPO == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 使用用户对应的盐值验证密码
        String inputPasswordHash = DigestUtil.sha256Hex(request.getPassword() + userPO.getSalt());
        if (!userPO.getPassword().equals(inputPasswordHash)) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (userPO.getStatus() != 1) {
            throw new BusinessException("账户已被禁用");
        }
        
        // 登录并生成 Token
        StpUtil.login(userPO.getId());
        
        // 构建响应
        return AuthDTO.builder()
                .token(StpUtil.getTokenValue())
                .code(userPO.getCode())
                .username(userPO.getUsername())
                .nickname(userPO.getNickname())
                .avatarUrl(userPO.getAvatarUrl())
                .build();
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
    }
}
