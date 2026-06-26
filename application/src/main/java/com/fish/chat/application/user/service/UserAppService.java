package com.fish.chat.application.user.service;

import com.fish.chat.application.user.dto.UserDTO;
import com.fish.chat.common.exception.AppException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.domain.user.model.entity.User;
import com.fish.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户应用服务
 * 
 * 职责:
 * - 流程编排
 * - 事务管理
 * - 权限校验（可选）
 * - 领域事件发布（可选）
 */
@Service
@RequiredArgsConstructor
public class UserAppService {
    
    private final UserRepository userRepository;
    
    /**
     * 根据用户 code 获取用户信息
     */
    public UserDTO getUserByCode(String code) {
        User user = userRepository.findByCode(code);
        if (user == null) {
            throw new AppException("用户不存在");
        }
        return toDTO(user);
    }
    
    /**
     * 更新用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDTO updateProfile(String userCode, String nickname, String avatarUrl, 
                                  String profile, String email, String mobile) {
        // 1. 查询实体
        User user = userRepository.findByCode(userCode);
        if (user == null) {
            throw new AppException("用户不存在");
        }
        
        // 2. 调用实体业务方法（业务规则校验在实体内部）
        user.updateProfile(nickname, avatarUrl, profile, email, mobile);
        
        // 3. 持久化
        userRepository.save(user);
        
        // 4. 返回 DTO
        return toDTO(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userCode, String oldPassword, String newPassword) {
        // 1. 查询实体
        User user = userRepository.findByCode(userCode);
        if (user == null) {
            throw new AppException("用户不存在");
        }
        
        // 2. 调用实体业务方法
        user.changePassword(oldPassword, newPassword);
        
        // 3. 持久化
        userRepository.save(user);
    }
    
    /**
     * 搜索用户
     */
    public PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize) {
        // 1. 查询总数
        long total = userRepository.countByKeyword(keyword);
        
        // 2. 查询数据
        List<User> users = userRepository.searchByKeyword(keyword, pageNum, pageSize);
        
        // 3. 转换为 DTO
        List<UserDTO> dtoList = new ArrayList<>();
        for (User user : users) {
            dtoList.add(toDTO(user));
        }
        
        return PageResult.of(dtoList, pageNum, pageSize, total);
    }
    
    /**
     * Entity → DTO
     */
    private UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setCode(user.getCode());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setProfile(user.getProfile());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
