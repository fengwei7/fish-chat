package com.fish.chat.application.user.service.impl;

import com.fish.chat.application.user.dto.UserDTO;
import com.fish.chat.application.user.service.UserAppService;
import com.fish.chat.common.exception.AppException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.domain.user.model.entity.User;
import com.fish.chat.domain.user.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {
    
    private final UserDomainService userDomainService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO register(String username, String password) {
        return null;
    }
    
    @Override
    public UserDTO login(String username, String password) {
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userCode, String oldPassword, String newPassword) {
        return;
    }
    
    @Override
    public UserDTO getCurrentUser(String userCode) {
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateCurrentUser(String userCode, String nickname, String avatarUrl, 
                                      String profile, String email, String mobile) {
        return null;
    }
    
    @Override
    public UserDTO getUserByCode(String code) {
        return null;
    }
    
    @Override
    public PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize) {
        return null;
    }
    
 }
