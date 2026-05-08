package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.chat.room.RoomManager;
import com.fish.chat.core.entity.dto.ChannelDTO;
import com.fish.chat.core.entity.po.ChannelMemberPO;
import com.fish.chat.core.entity.po.ChannelPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.repository.ChannelRepository;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.ChannelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChannelServiceImpl implements ChannelService {

    @Resource private ChannelRepository channelRepository;
    @Resource private UserRepository userRepository;
    @Resource private RoomManager roomManager;

    @Transactional
    @Override
    public ChannelDTO createChannel(String name, String avatar, String description) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO owner = resolveUser(userCode);

        ChannelPO channel = new ChannelPO();
        channel.setName(name);
        channel.setAvatar(avatar);
        channel.setDescription(description);
        channel.setOwnerCode(owner.getCode());
        channel.setStatus(1);
        channelRepository.save(channel);

        ChannelMemberPO member = new ChannelMemberPO();
        member.setChannelCode(channel.getCode());
        member.setUserCode(owner.getCode());
        member.setRole(2);
        member.setJoinTime(LocalDateTime.now());
        channelRepository.insertMember(member);

        roomManager.getOrCreateChannelRoom(channel.getCode(), channel.getName(), channel.getAvatar(), java.util.Collections.singleton(owner.getCode()));
        return toDTO(channel, owner.getCode(), 1);
    }

    @Override
    public ChannelDTO getChannel(String code) {
        ChannelPO c = channelRepository.selectByCode(code);
        if (c == null) throw new BusinessException("频道不存在");
        UserPO owner = userRepository.selectByCode(c.getOwnerCode());
        long count = channelRepository.countMembers(c.getCode());
        return toDTO(c, owner != null ? owner.getCode() : "", (int) count);
    }

    @Transactional
    @Override
    public void subscribe(String channelCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userCode);
        ChannelPO channel = channelRepository.selectByCode(channelCode);
        if (channel == null) throw new BusinessException("频道不存在");

        if (channelRepository.isMember(channel.getCode(), user.getCode())) {
            throw new BusinessException("已订阅该频道");
        }

        ChannelMemberPO member = new ChannelMemberPO();
        member.setChannelCode(channel.getCode());
        member.setUserCode(user.getCode());
        member.setRole(0);
        member.setJoinTime(LocalDateTime.now());
        channelRepository.insertMember(member);
    }

    @Transactional
    @Override
    public void unsubscribe(String channelCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userCode);
        ChannelPO channel = channelRepository.selectByCode(channelCode);
        if (channel == null) throw new BusinessException("频道不存在");

        channelRepository.deleteMember(channel.getCode(), user.getCode());
    }

    @Override
    public PageResult<ChannelDTO> listMyChannels(int pageNum, int pageSize) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userCode);

        Page<ChannelMemberPO> memberPage = channelRepository.selectMemberPage(
                user.getCode(), new Page<>(pageNum, pageSize));

        List<String> channelCodes = memberPage.getRecords().stream()
                .map(ChannelMemberPO::getChannelCode)
                .collect(Collectors.toList());

        if (channelCodes.isEmpty()) {
            return PageResult.of(new ArrayList<>(), pageNum, pageSize, memberPage.getTotal());
        }

        List<ChannelPO> channels = channelRepository.selectByCodes(channelCodes);
        Map<String, ChannelPO> channelMap = channels.stream()
                .collect(Collectors.toMap(ChannelPO::getCode, c -> c));

        List<ChannelDTO> result = new ArrayList<>();
        for (ChannelMemberPO member : memberPage.getRecords()) {
            ChannelPO c = channelMap.get(member.getChannelCode());
            if (c != null) {
                UserPO owner = userRepository.selectByCode(c.getOwnerCode());
                long count = channelRepository.countMembers(c.getCode());
                result.add(toDTO(c, owner != null ? owner.getCode() : "", (int) count));
            }
        }
        return PageResult.of(result, pageNum, pageSize, memberPage.getTotal());
    }

    @Override
    public PageResult<ChannelDTO> searchChannels(String keyword, int pageNum, int pageSize) {
        Page<ChannelPO> pageParam = new Page<>(pageNum, pageSize);
        Page<ChannelPO> pageResult = channelRepository.selectPage(pageParam, Wrappers.<ChannelPO>lambdaQuery()
                .like(ChannelPO::getName, keyword).eq(ChannelPO::getStatus, 1));
        List<ChannelDTO> list = pageResult.getRecords().stream()
                .map(c -> toDTO(c, "", 0)).collect(Collectors.toList());
        return PageResult.of(list, pageNum, pageSize, pageResult.getTotal());
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
