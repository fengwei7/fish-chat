package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.entity.dto.AuthDTO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.entity.req.LoginRequest;
import com.fish.chat.core.entity.req.RegisterRequest;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.AuthService;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean register(RegisterRequest request) {
        UserPO existingUserPO = userRepository.selectByUsername(request.getUsername());
        if (existingUserPO != null) {
            throw new BusinessException("用户名已存在");
        }

        String salt = RandomUtil.randomString(16);
        UserPO userPO = new UserPO();
        userPO.setUsername(request.getUsername());
        userPO.setPassword(DigestUtil.sha256Hex(request.getPassword() + salt));
        userPO.setCode(UUID.randomUUID().toString());
        userPO.setSalt(salt);
        userPO.setMobile(request.getMobile());
        userPO.setEmail(request.getEmail());
        userPO.setNickname(request.getNickname());
        userPO.setStatus(1);

        userRepository.save(userPO);
        return true;
    }

    @Override
    public AuthDTO login(LoginRequest request) {
        UserPO userPO = userRepository.selectByUsername(request.getUsername());
        if (userPO == null) {
            throw new BusinessException("用户名或密码错误");
        }

        String inputPasswordHash = DigestUtil.sha256Hex(request.getPassword() + userPO.getSalt());
        if (!userPO.getPassword().equals(inputPasswordHash)) {
            throw new BusinessException("用户名或密码错误");
        }

        if (userPO.getStatus() != 1) {
            throw new BusinessException("账户已被禁用");
        }

        StpUtil.login(userPO.getId());

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
}
