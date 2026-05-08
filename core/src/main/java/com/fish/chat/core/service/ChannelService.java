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
}