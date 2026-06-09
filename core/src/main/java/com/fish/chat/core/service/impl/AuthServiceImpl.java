package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.UUID;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.entity.dto.AuthDTO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.req.RegisterRequest;
import com.fish.chat.core.enums.CommonStatus;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.AuthService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean register(RegisterRequest request) {
        UserPO existingUserPO = userRepository.selectByUsername(request.getUsername());
        if (existingUserPO != null) {
            throw new BusinessException("用户名已存在");
        }

        UserPO userPO = new UserPO();
        userPO.setUsername(request.getUsername());
        // 使用 BCrypt 加密密码（自动生成 salt 并嵌入到哈希中）
        userPO.setPassword(passwordEncoder.encode(request.getPassword()));
        userPO.setCode(UUID.randomUUID().toString());
        userPO.setMobile(request.getMobile());
        userPO.setEmail(request.getEmail());
        userPO.setNickname(request.getNickname());
        userPO.setStatus(CommonStatus.NORMAL.getValue());

        userRepository.save(userPO);
        return true;
    }

    @Override
    public AuthDTO login(LoginRequest request) {
        UserPO userPO = userRepository.selectByUsername(request.getUsername());
        if (userPO == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 使用 BCrypt 验证密码
        if (!passwordEncoder.matches(request.getPassword(), userPO.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!CommonStatus.NORMAL.getValue().equals(userPO.getStatus())) {
            throw new BusinessException("账户已被禁用");
        }

        StpUtil.login(userPO.getCode());

        return AuthDTO.builder()
                .token(StpUtil.getTokenValue())
                .code(userPO.getCode())
                .username(userPO.getUsername())
                .nickname(userPO.getNickname())
                .avatarUrl(userPO.getAvatarUrl())
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO user = userRepository.selectByCode(userCode);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 1. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 2. 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.updateById(user);

        // 3. 登出当前用户（强制重新登录）
        StpUtil.logout();
    }
}
