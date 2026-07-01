package com.fish.chat.domain.user.service;

import com.fish.chat.domain.user.model.entity.User;

import java.util.List;

/**
 * 用户领域服务接口
 * 
 * 职责：
 * - 纯业务逻辑，不包含事务管理
 * - 操作领域实体（User Entity）
 * - 不包含 DTO 转换
 * 
 * 设计原则：
 * - 无状态：所有方法都是纯函数
 * - 领域规则：包含用户相关的业务规则验证
 * - 可复用：可被多个 AppService 调用
 */
public interface UserDomainService {
    
    // ==================== 用户查询 ====================
    
    /**
     * 根据用户 Code 查询用户
     * 
     * @param code 用户业务编码
     * @return 用户实体，不存在时返回 null
     */
    User findByCode(String code);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    User findByUsername(String username);
    
    /**
     * 根据 ID 查询用户
     * 
     * @param id 用户 ID
     * @return 用户实体，不存在时返回 null
     */
    User findById(Long id);
    
    /**
     * 根据 ID 查询用户（不存在则抛异常）
     * 
     * @param id 用户 ID
     * @return 用户实体
     * @throws com.fish.chat.common.exception.DomainException 当用户不存在时
     */
    User findByIdOrThrow(Long id);
    
    // ==================== 用户操作 ====================
    
    /**
     * 创建用户
     * 
     * 业务规则：
     * - 用户名不能为空
     * - 密码长度不能少于 6 个字符
     * - 用户名不能重复
     * 
     * @param username 用户名
     * @param password 密码（明文）
     * @return 创建后的用户实体
     * @throws com.fish.chat.common.exception.DomainException 当用户名已存在时
     */
    User createUser(String username, String password);
    
    /**
     * 更新用户资料
     * 
     * 业务规则：
     * - 昵称长度不能超过 50 个字符
     * - 邮箱格式必须正确
     * - 手机号格式必须正确
     * 
     * @param user 用户实体
     * @param nickname 昵称（可为 null）
     * @param avatarUrl 头像 URL（可为 null）
     * @param profile 个人简介（可为 null）
     * @param email 邮箱（可为 null）
     * @param mobile 手机号（可为 null）
     */
    void updateUserProfile(User user, String nickname, String avatarUrl, String profile, String email, String mobile);
    
    /**
     * 修改密码
     * 
     * 业务规则：
     * - 必须验证原密码
     * - 新密码长度不能少于 6 个字符
     * 
     * @param user 用户实体
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @throws com.fish.chat.common.exception.DomainException 当原密码错误时
     */
    void changeUserPassword(User user, String oldPassword, String newPassword);
    
    /**
     * 验证用户密码
     * 
     * @param user 用户实体
     * @param password 待验证的密码
     * @return true-密码正确，false-密码错误
     */
    boolean verifyUserPassword(User user, String password);
    
    // ==================== 用户搜索 ====================
    
    /**
     * 搜索用户（用户名或昵称模糊匹配）
     * 
     * @param keyword 搜索关键字
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 用户实体列表
     */
    List<User> searchUsers(String keyword, int pageNum, int pageSize);
    
    /**
     * 统计关键字匹配的用户数
     * 
     * @param keyword 搜索关键字
     * @return 用户总数
     */
    long countUsersByKeyword(String keyword);
    
    // ==================== 用户存在性检查 ====================
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 保存用户（新增或更新）
     * 
     * @param user 用户实体
     * @return 保存后的用户实体（包含回填的 ID）
     */
    User saveUser(User user);
}
