package com.fish.chat.application.user.service;

import com.fish.chat.application.user.dto.UserDTO;
import com.fish.chat.common.result.PageResult;

import java.util.Map;


public interface UserAppService {

    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @return 注册后的用户 DTO
     */
    UserDTO register(String username, String password);
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果（包含 token 和用户信息）
     */
    UserDTO login(String username, String password);
    
    /**
     * 修改密码
     * 
     * @param userCode 用户编码
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void changePassword(String userCode, String oldPassword, String newPassword);
    
    // ==================== 用户信息管理 ====================
    
    /**
     * 获取当前用户信息
     * 
     * @param userCode 用户编码
     * @return 用户 DTO
     */
    UserDTO getCurrentUser(String userCode);
    
    /**
     * 更新当前用户信息
     * 
     * @param userCode 用户编码
     * @param nickname 昵称
     * @param avatarUrl 头像 URL
     * @param profile 个人简介
     * @param email 邮箱
     * @param mobile 手机号
     * @return 更新后的用户 DTO
     */
    UserDTO updateCurrentUser(String userCode, String nickname, String avatarUrl, 
                              String profile, String email, String mobile);
    
    /**
     * 根据用户 Code 获取用户信息
     * 
     * @param code 用户编码
     * @return 用户 DTO
     */
    UserDTO getUserByCode(String code);
    
    // ==================== 用户搜索 ====================
    
    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页的用户搜索结果
     */
    PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize);
    
}
