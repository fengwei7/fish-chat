package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.FriendDTO;

/**
 * 好友服务接口
 * 
 * 提供好友添加、删除、查询、好友请求处理等功能
 * 好友关系是双向的，添加好友后会创建两条好友记录
 * 
 * @author fengwei
 * @since 2026-06-09
 */
public interface FriendService {
    
    /**
     * 添加好友
     * 
     * 发送好友请求给对方，状态为待确认
     *
     * @param friendCode 目标用户的用户code
     * @param remark 好友备注（可选）
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在、添加自己或已发送请求时抛出
     */
    void addFriend(String friendCode, String remark);
    
    /**
     * 接受好友请求
     * 
     * 确认好友关系，同时创建双向好友记录
     *
     * @param friendCode 发送好友请求的用户的用户code
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在或没有待确认的请求时抛出
     */
    void acceptFriend(String friendCode);
    
    /**
     * 删除好友
     * 
     * 删除双向好友关系
     *
     * @param friendCode 目标用户的用户code
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在时抛出
     */
    void removeFriend(String friendCode);
    
    /**
     * 查询我的好友列表（分页）
     *
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的好友列表，包含在线状态
     */
    PageResult<FriendDTO> listFriends(int pageNum, int pageSize);
    
    /**
     * 查询收到的好友请求列表（分页）
     *
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的好友请求列表，状态为待确认
     */
    PageResult<FriendDTO> listFriendRequests(int pageNum, int pageSize);
    
    /**
     * 拒绝好友申请
     * 
     * 将好友请求状态更新为已拒绝
     *
     * @param friendCode 发送好友请求的用户的用户code
     * @throws com.fish.chat.common.exception.BusinessException 当用户不存在或没有待确认的请求时抛出
     */
    void rejectFriend(String friendCode);
    
    /**
     * 修改好友备注
     * 
     * 更新指定好友的备注名称
     *
     * @param friendCode 目标用户的用户code
     * @param remark 好友备注（支持空字符串清空备注）
     * @throws com.fish.chat.common.exception.BusinessException 当好友不存在时抛出
     */
    void updateFriendRemark(String friendCode, String remark);
    
    /**
     * 搜索用户（按code或用户名模糊匹配）
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的用户搜索结果
     */
    PageResult<FriendDTO> searchUsers(String keyword, int pageNum, int pageSize);
}