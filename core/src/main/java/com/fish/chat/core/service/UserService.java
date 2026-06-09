package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.req.UserUpdateRequest;

/**
 * 用户服务接口
 * 
 * 提供用户信息查询、更新、搜索等功能
 * 
 * @author fengwei
 * @since 2026-06-09
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户的详细信息DTO
     * @throws cn.dev33.satoken.exception.NotLoginException 当用户未登录时抛出
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在时抛出
     */
    UserDTO getCurrentUser();

    /**
     * 更新当前登录用户信息
     * 
     * 仅更新请求中非空的字段
     *
     * @param request 用户更新请求，包含昵称、头像、个性签名等
     * @return 更新后的用户信息DTO
     * @throws cn.dev33.satoken.exception.NotLoginException 当用户未登录时抛出
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在时抛出
     */
    UserDTO updateCurrentUser(UserUpdateRequest request);

    /**
     * 根据用户code查看用户信息
     *
     * @param code 用户唯一标识
     * @return 用户信息DTO
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在时抛出
     */
    UserDTO getUserByCode(String code);

    /**
     * 搜索用户
     * 
     * 按用户名或昵称进行模糊匹配
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的用户搜索结果
     */
    PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize);
}
