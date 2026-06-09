package com.fish.chat.core.service;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.entity.dto.ChannelDTO;

public interface ChannelService {
    ChannelDTO createChannel(String name, String avatar, String description);
    ChannelDTO getChannel(String code);
    void subscribe(String channelCode);
    void unsubscribe(String channelCode);
    PageResult<ChannelDTO> listMyChannels(int pageNum, int pageSize);
    PageResult<ChannelDTO> searchChannels(String keyword, int pageNum, int pageSize);
    
    /**
     * 转让频道（创建者将 OWNER 角色转让给管理员）
     * 
     * @param channelCode 频道code
     * @param newOwnerCode 新创建者code
     */
    void transferChannel(String channelCode, String newOwnerCode);
    
    /**
     * 设置/取消管理员
     * 
     * @param channelCode 频道code
     * @param userCode 用户code
     * @param isAdmin true=设置为管理员，false=取消管理员
     */
    void setAdmin(String channelCode, String userCode, boolean isAdmin);
}