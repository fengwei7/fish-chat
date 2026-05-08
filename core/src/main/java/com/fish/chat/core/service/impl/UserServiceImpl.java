package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.entity.req.UserUpdateRequest;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize) {
        Page<UserPO> userPage = userRepository.searchByKeywordPage(keyword, new Page<>(pageNum, pageSize));
        List<UserDTO> result = new ArrayList<>();
        for (UserPO po : userPage.getRecords()) {
            result.add(toDTO(po));
        }
        return PageResult.of(result, pageNum, pageSize, userPage.getTotal());
    }

    private UserPO getCurrentUserPO() {
        String loginId = StpUtil.getLoginIdAsString();
        UserPO userPO = userRepository.selectByCode(loginId);
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
