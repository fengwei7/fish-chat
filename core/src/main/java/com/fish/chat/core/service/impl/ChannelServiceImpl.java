package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.core.netty.chat.room.RoomManager;
import com.fish.chat.core.entity.dto.ChannelDTO;
import com.fish.chat.core.entity.po.ChannelMemberPO;
import com.fish.chat.core.entity.po.ChannelPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.enums.CommonStatus;
import com.fish.chat.core.enums.MemberRole;
import com.fish.chat.core.repository.ChannelRepository;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
        channel.setStatus(CommonStatus.NORMAL.getValue());
        channelRepository.save(channel);

        ChannelMemberPO member = new ChannelMemberPO();
        member.setChannelCode(channel.getCode());
        member.setUserCode(owner.getCode());
        member.setRole(MemberRole.OWNER.getValue());
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
        member.setRole(MemberRole.MEMBER.getValue());
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
                .like(ChannelPO::getName, keyword).eq(ChannelPO::getStatus, CommonStatus.NORMAL.getValue()));
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void transferChannel(String channelCode, String newOwnerCode) {
        String currentUserCode = StpUtil.getLoginIdAsString();
        ChannelPO channel = channelRepository.selectByCode(channelCode);
        if (channel == null) throw new BusinessException("频道不存在");

        // 验证当前用户是创建者
        validateChannelOwner(channelCode, currentUserCode);

        // 检查新创建者是否是频道成员
        Integer newOwnerRole = channelRepository.getMemberRole(channelCode, newOwnerCode);
        if (newOwnerRole == null) {
            throw new BusinessException("无法转让频道：该用户未订阅此频道");
        }

        // 不能转让给自己
        if (currentUserCode.equals(newOwnerCode)) {
            throw new BusinessException("无法转让频道：不能转让给自己");
        }

        log.info("开始转让频道: channelCode={}, 原创建者={}, 新创建者={}", 
                channelCode, currentUserCode, newOwnerCode);

        // 在事务中更新：先更新新创建者为OWNER，再更新原创建者为ADMIN
        // 如果任何一步失败，整个事务会回滚
        channelRepository.updateMemberRole(channelCode, newOwnerCode, MemberRole.OWNER.getValue());
        channelRepository.updateMemberRole(channelCode, currentUserCode, MemberRole.ADMIN.getValue());

        // 更新频道的 owner_code
        channel.setOwnerCode(newOwnerCode);
        channelRepository.updateById(channel);

        log.info("频道转让成功: channelCode={}, 原创建者={}, 新创建者={}", 
                channelCode, currentUserCode, newOwnerCode);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setAdmin(String channelCode, String userCode, boolean isAdmin) {
        String currentUserCode = StpUtil.getLoginIdAsString();
        ChannelPO channel = channelRepository.selectByCode(channelCode);
        if (channel == null) throw new BusinessException("频道不存在");

        // 验证当前用户是创建者
        validateChannelOwner(channelCode, currentUserCode);

        // 不能修改自己的角色
        if (currentUserCode.equals(userCode)) {
            throw new BusinessException("无法修改自己的角色");
        }

        // 检查目标用户是否是频道成员
        Integer targetRole = channelRepository.getMemberRole(channelCode, userCode);
        if (targetRole == null) {
            throw new BusinessException("无法设置管理员：该用户未订阅此频道");
        }

        // 不能修改创建者的角色
        if (MemberRole.OWNER.getValue().equals(targetRole)) {
            throw new BusinessException("无法修改创建者的角色");
        }

        String action = isAdmin ? "设置为管理员" : "取消管理员";
        log.info("{}: channelCode={}, 操作者={}, 目标用户={}", 
                action, channelCode, currentUserCode, userCode);

        // 设置或取消管理员
        Integer newRole = isAdmin ? MemberRole.ADMIN.getValue() : MemberRole.MEMBER.getValue();
        channelRepository.updateMemberRole(channelCode, userCode, newRole);

        log.info("{}成功: channelCode={}, 操作者={}, 目标用户={}", 
                action, channelCode, currentUserCode, userCode);
    }

    /**
     * 验证用户是否是频道创建者
     */
    private void validateChannelOwner(String channelCode, String userCode) {
        Integer role = channelRepository.getMemberRole(channelCode, userCode);
        if (!MemberRole.OWNER.getValue().equals(role)) {
            throw new BusinessException("仅频道创建者可以执行此操作");
        }
    }
}
