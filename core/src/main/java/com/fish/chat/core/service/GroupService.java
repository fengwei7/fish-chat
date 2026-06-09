package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.GroupDTO;

/**
 * 群组服务接口
 * 
 * 提供群组的创建、管理、成员管理、查询等核心功能
 * 群组支持多对多通信，所有成员都可以发言
 * 
 * @author fengwei
 * @since 2026-06-09
 */
public interface GroupService {
    
    /**
     * 创建群组
     * 
     * 创建者自动成为群主并加入群组
     *
     * @param name 群组名称
     * @param avatar 群组头像URL
     * @return 创建后的群组信息DTO
     * @throws com.fish.chat.common.exception.BusinessException 当参数不合法时抛出
     */
    GroupDTO createGroup(String name, String avatar);
    
    /**
     * 根据群组code获取群组详情
     *
     * @param code 群组唯一标识
     * @return 群组信息DTO
     * @throws com.fish.chat.common.exception.BusinessException 当群组不存在时抛出
     */
    GroupDTO getGroup(String code);
    
    /**
     * 解散群组
     * 
     * 仅群主可以执行此操作，群组状态变为已解散
     *
     * @param code 群组唯一标识
     * @throws com.fish.chat.common.exception.BusinessException 当群组不存在或用户不是群主时抛出
     */
    void dismissGroup(String code);
    
    /**
     * 添加群组成员
     *
     * @param groupCode 群组唯一标识
     * @param userCode 要添加的用户的用户code
     * @throws com.fish.chat.common.exception.BusinessException 当群组不存在、用户不存在或用户已在群中时抛出
     */
    void addMember(String groupCode, String userCode);
    
    /**
     * 移除群组成员
     *
     * @param groupCode 群组唯一标识
     * @param userCode 要移除的用户的用户code
     * @throws com.fish.chat.common.exception.BusinessException 当群组不存在或用户不存在时抛出
     */
    void removeMember(String groupCode, String userCode);
    
    /**
     * 查询我加入的群组列表（分页）
     *
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的群组列表
     */
    PageResult<GroupDTO> listMyGroups(int pageNum, int pageSize);
    
    /**
     * 搜索群组（按名称模糊匹配）
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的群组搜索结果
     */
    PageResult<GroupDTO> searchGroups(String keyword, int pageNum, int pageSize);
}