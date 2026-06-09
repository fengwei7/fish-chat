package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.ChannelDTO;

/**
 * 频道服务接口
 * 
 * 提供频道的创建、查询、订阅、管理等核心功能
 * 频道是一种一对多的广播式通信模式，仅管理员和创建者可发言
 * 
 * @author fengwei
 * @since 2026-06-09
 */
public interface ChannelService {
    
    /**
     * 创建频道
     *
     * @param name 频道名称
     * @param avatar 频道头像URL
     * @param description 频道描述
     * @return 创建后的频道信息DTO
     * @throws com.fish.chat.common.exception.BusinessException 当参数不合法时抛出
     */
    ChannelDTO createChannel(String name, String avatar, String description);
    
    /**
     * 根据频道code获取频道详情
     *
     * @param code 频道唯一标识
     * @return 频道信息DTO
     * @throws com.fish.chat.common.exception.BusinessException 当频道不存在时抛出
     */
    ChannelDTO getChannel(String code);
    
    /**
     * 订阅频道
     * 
     * 当前登录用户订阅指定频道，成为频道成员
     *
     * @param channelCode 频道唯一标识
     * @throws com.fish.chat.common.exception.BusinessException 当频道不存在或用户已订阅时抛出
     */
    void subscribe(String channelCode);
    
    /**
     * 取消订阅频道
     * 
     * 当前登录用户取消订阅指定频道，从频道成员中移除
     *
     * @param channelCode 频道唯一标识
     * @throws com.fish.chat.common.exception.BusinessException 当频道不存在时抛出
     */
    void unsubscribe(String channelCode);
    
    /**
     * 查询当前用户订阅的频道列表（分页）
     *
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的频道列表
     */
    PageResult<ChannelDTO> listMyChannels(int pageNum, int pageSize);
    
    /**
     * 搜索频道（按名称模糊匹配）
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码，从0开始
     * @param pageSize 每页数量
     * @return 分页的频道搜索结果
     */
    PageResult<ChannelDTO> searchChannels(String keyword, int pageNum, int pageSize);
    
    /**
     * 转让频道
     * 
     * 频道创建者将频道所有权转让给其他频道成员
     * 转让后，原创建者降为管理员，新成员成为创建者
     *
     * @param channelCode 频道唯一标识
     * @param newOwnerCode 新创建者的用户code
     * @throws com.fish.chat.common.exception.BusinessException 当频道不存在、用户无权限或新创建者不是频道成员时抛出
     */
    void transferChannel(String channelCode, String newOwnerCode);
    
    /**
     * 设置/取消频道管理员
     * 
     * 仅频道创建者可以执行此操作
     *
     * @param channelCode 频道唯一标识
     * @param userCode 目标用户的用户code
     * @param isAdmin true表示设置为管理员，false表示取消管理员
     * @throws com.fish.chat.common.exception.BusinessException 当频道不存在、用户无权限或目标用户不是频道成员时抛出
     */
    void setAdmin(String channelCode, String userCode, boolean isAdmin);
}