package com.fish.chat.core.service;

import com.fish.chat.core.entity.dto.ChannelDTO;
import java.util.List;

public interface ChannelService {
    ChannelDTO createChannel(String name, String avatar, String description);
    ChannelDTO getChannel(String code);
    void subscribe(String channelCode);
    void unsubscribe(String channelCode);
    List<ChannelDTO> listMyChannels();
    List<ChannelDTO> searchChannels(String keyword);
}