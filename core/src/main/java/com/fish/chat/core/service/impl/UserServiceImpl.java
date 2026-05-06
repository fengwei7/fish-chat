package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.entity.req.UserUpdateRequest;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    public UserDTO getCurrentUser() {
        UserPO userPO = getCurrentUserPO();
        return toDTO(userPO);
    }

    @Override
    public UserDTO updateCurrentUser(UserUpdateRequest request) {
        UserPO userPO = getCurrentUserPO();

        if (request.getNickname() != null) {
            userPO.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            userPO.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getProfile() != null) {
            userPO.setProfile(request.getProfile());
        }
        if (request.getEmail() != null) {
            userPO.setEmail(request.getEmail());
        }
        if (request.getMobile() != null) {
            userPO.setMobile(request.getMobile());
        }

        userRepository.updateById(userPO);
        return toDTO(userPO);
    }

    @Override
    public UserDTO getUserByCode(String code) {
        UserPO userPO = userRepository.selectByCode(code);
        if (userPO == null) {
            throw new BusinessException("用户不存在");
        }
        return toDTO(userPO);
    }

    private UserPO getCurrentUserPO() {
        String loginId = StpUtil.getLoginIdAsString();
        UserPO userPO = userRepository.selectById(loginId);
        if (userPO == null) {
            throw new BusinessException("用户不存在");
        }
        return userPO;
    }

    private UserDTO toDTO(UserPO userPO) {
        UserDTO dto = new UserDTO();
        dto.setCode(userPO.getCode());
        dto.setUsername(userPO.getUsername());
        dto.setNickname(userPO.getNickname());
        dto.setAvatarUrl(userPO.getAvatarUrl());
        dto.setProfile(userPO.getProfile());
        dto.setEmail(userPO.getEmail());
        dto.setMobile(userPO.getMobile());
        return dto;
    }
}
