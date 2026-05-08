package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.UserDTO;
import com.fish.chat.core.entity.req.UserUpdateRequest;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息 DTO
     */
    UserDTO getCurrentUser();

    /**
     * 更新当前用户信息（仅更新非空字段）
     *
     * @param request 用户更新请求
     * @return 更新后的用户信息
     */
    UserDTO updateCurrentUser(UserUpdateRequest request);

    /**
     * 根据用户 code 查看用户信息
     *
     * @param code 用户 code
     * @return 用户信息 DTO
     */
    UserDTO getUserByCode(String code);

    /**
     * 搜索用户（按用户名/昵称模糊匹配）
     *
     * @param keyword  关键词
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<UserDTO> searchUsers(String keyword, int pageNum, int pageSize);
}
