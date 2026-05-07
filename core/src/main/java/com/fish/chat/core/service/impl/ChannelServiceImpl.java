package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.chat.room.RoomManager;
import com.fish.chat.core.entity.dto.ChannelDTO;
import com.fish.chat.core.entity.po.ChannelMemberPO;
import com.fish.chat.core.entity.po.ChannelPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.mapper.ChannelMapper;
import com.fish.chat.core.mapper.ChannelMemberMapper;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.ChannelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChannelServiceImpl implements ChannelService {

    @Resource private ChannelMapper channelMapper;
    @Resource private ChannelMemberMapper channelMemberMapper;
    @Resource private UserRepository userRepository;
    @Resource private RoomManager roomManager;

    @Transactional
    @Override
    public ChannelDTO createChannel(String name, String avatar, String description) {
        String userId = StpUtil.getLoginIdAsString();
        UserPO owner = resolveUser(userId);

        ChannelPO channel = new ChannelPO();
        channel.setName(name);
        channel.setAvatar(avatar);
        channel.setDescription(description);
        channel.setOwnerId(owner.getId());
        channel.setStatus(1);
        channelMapper.insert(channel);

        ChannelMemberPO member = new ChannelMemberPO();
        member.setChannelId(channel.getId());
        member.setUserId(owner.getId());
        member.setRole(2);
        member.setJoinTime(LocalDateTime.now());
        channelMemberMapper.insert(member);

        roomManager.getOrCreateChannelRoom(channel.getCode(), channel.getName(), channel.getAvatar(), java.util.Collections.singleton(owner.getCode()));
        return toDTO(channel, owner.getCode(), 1);
    }

    @Override
    public ChannelDTO getChannel(String code) {
        ChannelPO c = channelMapper.selectOne(Wrappers.<ChannelPO>lambdaQuery().eq(ChannelPO::getCode, code));
        if (c == null) throw new BusinessException("频道不存在");
        UserPO owner = userRepository.selectById(String.valueOf(c.getOwnerId()));
        long count = channelMemberMapper.selectCount(Wrappers.<ChannelMemberPO>lambdaQuery().eq(ChannelMemberPO::getChannelId, c.getId()));
        return toDTO(c, owner != null ? owner.getCode() : "", (int) count);
    }

    @Transactional
    @Override
    public void subscribe(String channelCode) {
        String userId = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userId);
        ChannelPO channel = channelMapper.selectOne(Wrappers.<ChannelPO>lambdaQuery().eq(ChannelPO::getCode, channelCode));
        if (channel == null) throw new BusinessException("频道不存在");

        Long count = channelMemberMapper.selectCount(Wrappers.<ChannelMemberPO>lambdaQuery()
                .eq(ChannelMemberPO::getChannelId, channel.getId())
                .eq(ChannelMemberPO::getUserId, user.getId()));
        if (count > 0) throw new BusinessException("已订阅该频道");

        ChannelMemberPO member = new ChannelMemberPO();
        member.setChannelId(channel.getId());
        member.setUserId(user.getId());
        member.setRole(0);
        member.setJoinTime(LocalDateTime.now());
        channelMemberMapper.insert(member);
    }

    @Transactional
    @Override
    public void unsubscribe(String channelCode) {
        String userId = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userId);
        ChannelPO channel = channelMapper.selectOne(Wrappers.<ChannelPO>lambdaQuery().eq(ChannelPO::getCode, channelCode));
        if (channel == null) throw new BusinessException("频道不存在");

        channelMemberMapper.delete(Wrappers.<ChannelMemberPO>lambdaQuery()
                .eq(ChannelMemberPO::getChannelId, channel.getId())
                .eq(ChannelMemberPO::getUserId, user.getId()));
    }

    @Override
    public List<ChannelDTO> listMyChannels() {
        String userId = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userId);
        List<ChannelMemberPO> subs = channelMemberMapper.selectList(
                Wrappers.<ChannelMemberPO>lambdaQuery().eq(ChannelMemberPO::getUserId, user.getId()));
        List<ChannelDTO> result = new ArrayList<>();
        for (ChannelMemberPO sub : subs) {
            ChannelPO c = channelMapper.selectById(sub.getChannelId());
            if (c != null && c.getStatus() == 1) {
                UserPO owner = userRepository.selectById(String.valueOf(c.getOwnerId()));
                long count = channelMemberMapper.selectCount(Wrappers.<ChannelMemberPO>lambdaQuery().eq(ChannelMemberPO::getChannelId, c.getId()));
                result.add(toDTO(c, owner != null ? owner.getCode() : "", (int) count));
            }
        }
        return result;
    }

    @Override
    public List<ChannelDTO> searchChannels(String keyword) {
        return channelMapper.selectList(Wrappers.<ChannelPO>lambdaQuery()
                .like(ChannelPO::getName, keyword).eq(ChannelPO::getStatus, 1))
                .stream().map(c -> toDTO(c, "", 0)).collect(Collectors.toList());
    }

    private UserPO resolveUser(String loginId) {
        UserPO u = userRepository.selectByCode(loginId);
        if (u == null) u = userRepository.selectById(loginId);
        if (u == null) throw new BusinessException("用户不存在");
        return u;
    }

    private ChannelDTO toDTO(ChannelPO c, String ownerCode, int subs) {
        ChannelDTO dto = new ChannelDTO();
        dto.setCode(c.getCode());
        dto.setName(c.getName());
        dto.setAvatar(c.getAvatar());
        dto.setOwnerCode(ownerCode);
        dto.setDescription(c.getDescription());
        dto.setSubscriberCount(subs);
        dto.setStatus(c.getStatus());
        return dto;
    }
}
