package com.fish.chat.domain.user.service.impl;

import com.fish.chat.common.exception.AppException;
import com.fish.chat.domain.user.model.entity.User;
import com.fish.chat.domain.user.repository.UserRepository;
import com.fish.chat.domain.user.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户领域服务实现
 * 
 * 职责：
 * - 实现纯业务逻辑
 * - 不包含事务管理（由 AppService 管理）
 * - 不包含 DTO 转换（由 AppService 转换）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {
    
    private final UserRepository userRepository;
    
    @Override
    public User findByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        return userRepository.findByCode(code);
    }
    
    @Override
    public User findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return userRepository.findByUsername(username);
    }
    
    @Override
    public User findById(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id);
    }
    
    @Override
    public User findByIdOrThrow(Long id) {
        User user = findById(id);
        if (user == null) {
            throw new AppException("用户不存在，ID: " + id);
        }
        return user;
    }
    
    @Override
    public User createUser(String username, String password) {
        // 检查用户名是否已存在
        if (existsByUsername(username)) {
            throw new AppException("用户名已存在: " + username);
        }
        
        // 使用工厂方法创建用户（包含密码加密等逻辑）
        User user = User.create(username, password);
        
        // 保存用户
        return saveUser(user);
    }
    
    @Override
    public void updateUserProfile(User user, String nickname, String avatarUrl, 
                                   String profile, String email, String mobile) {
        // 调用实体方法，由实体执行验证逻辑
        user.updateProfile(nickname, avatarUrl, profile, email, mobile);
        
        // 持久化
        userRepository.save(user);
        
        log.debug("用户资料更新成功，userCode: {}", user.getCode());
    }
    
    @Override
    public void changeUserPassword(User user, String oldPassword, String newPassword) {
        // 调用实体方法，由实体执行密码验证逻辑
        user.changePassword(oldPassword, newPassword);
        
        // 持久化
        userRepository.save(user);
        
        log.debug("用户密码修改成功，userCode: {}", user.getCode());
    }
    
    @Override
    public boolean verifyUserPassword(User user, String password) {
        return user.verifyPassword(password);
    }
    
    @Override
    public List<User> searchUsers(String keyword, int pageNum, int pageSize) {
        return userRepository.searchByKeyword(keyword, pageNum, pageSize);
    }
    
    @Override
    public long countUsersByKeyword(String keyword) {
        return userRepository.countByKeyword(keyword);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
