package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.chat.room.Room;
import com.fish.chat.core.chat.room.RoomManager;
import com.fish.chat.core.entity.dto.GroupDTO;
import com.fish.chat.core.entity.po.GroupMemberPO;
import com.fish.chat.core.entity.po.GroupPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.mapper.GroupMapper;
import com.fish.chat.core.mapper.GroupMemberMapper;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    @Resource private GroupMapper groupMapper;
    @Resource private GroupMemberMapper groupMemberMapper;
    @Resource private UserRepository userRepository;
    @Resource private RoomManager roomManager;

    @Transactional
    @Override
    public GroupDTO createGroup(String name, String avatar) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO owner = resolveUser(userCode);

        GroupPO group = new GroupPO();
        group.setName(name);
        group.setAvatar(avatar);
        group.setOwnerCode(owner.getCode());
        group.setMaxMembers(200);
        group.setStatus(1);
        groupMapper.insert(group);
        // BasePO auto-fill sets code

        // 创建者自动加入
        GroupMemberPO member = new GroupMemberPO();
        member.setGroupCode(group.getCode());
        member.setUserCode(owner.getCode());
        member.setRole(2); // 群主
        member.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(member);

        // 加载到 RoomManager
        java.util.Set<String> members = java.util.Collections.singleton(owner.getCode());
        roomManager.getOrCreateGroupRoom(group.getCode(), group.getName(), group.getAvatar(), members);

        return toDTO(group, owner.getCode(), 1);
    }

    @Override
    public GroupDTO getGroup(String code) {
        GroupPO group = groupMapper.selectOne(Wrappers.<GroupPO>lambdaQuery().eq(GroupPO::getCode, code));
        if (group == null) throw new BusinessException("群组不存在");

        UserPO owner = userRepository.selectByCode(group.getOwnerCode());
        long count = groupMemberMapper.selectCount(Wrappers.<GroupMemberPO>lambdaQuery().eq(GroupMemberPO::getGroupCode, group.getCode()));

        return toDTO(group, owner != null ? owner.getCode() : "", (int) count);
    }

    @Transactional
    @Override
    public void dismissGroup(String code) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO current = resolveUser(userCode);

        GroupPO group = groupMapper.selectOne(Wrappers.<GroupPO>lambdaQuery().eq(GroupPO::getCode, code));
        if (group == null) throw new BusinessException("群组不存在");
        if (!group.getOwnerCode().equals(current.getCode())) throw new BusinessException("只有群主可以解散群组");

        group.setStatus(0);
        groupMapper.updateById(group);
    }

    @Transactional
    @Override
    public void addMember(String groupCode, String userCode) {
        GroupPO group = groupMapper.selectOne(Wrappers.<GroupPO>lambdaQuery().eq(GroupPO::getCode, groupCode));
        if (group == null) throw new BusinessException("群组不存在");

        UserPO user = userRepository.selectByCode(userCode);
        if (user == null) throw new BusinessException("用户不存在");

        // 检查是否已加入
        Long count = groupMemberMapper.selectCount(Wrappers.<GroupMemberPO>lambdaQuery()
                .eq(GroupMemberPO::getGroupCode, group.getCode())
                .eq(GroupMemberPO::getUserCode, user.getCode()));
        if (count > 0) throw new BusinessException("用户已在群中");

        GroupMemberPO member = new GroupMemberPO();
        member.setGroupCode(group.getCode());
        member.setUserCode(user.getCode());
        member.setRole(0);
        member.setJoinTime(LocalDateTime.now());
        groupMemberMapper.insert(member);

        // 更新 Room
        roomManager.addMemberToGroup(groupCode, userCode);
    }

    @Transactional
    @Override
    public void removeMember(String groupCode, String userCode) {
        GroupPO group = groupMapper.selectOne(Wrappers.<GroupPO>lambdaQuery().eq(GroupPO::getCode, groupCode));
        if (group == null) throw new BusinessException("群组不存在");

        UserPO user = userRepository.selectByCode(userCode);
        if (user == null) throw new BusinessException("用户不存在");

        groupMemberMapper.delete(Wrappers.<GroupMemberPO>lambdaQuery()
                .eq(GroupMemberPO::getGroupCode, group.getCode())
                .eq(GroupMemberPO::getUserCode, user.getCode()));

        roomManager.removeMemberFromGroup(groupCode, userCode);
    }

    @Override
    public List<GroupDTO> listMyGroups() {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userCode);

        List<GroupMemberPO> memberships = groupMemberMapper.selectList(
                Wrappers.<GroupMemberPO>lambdaQuery().eq(GroupMemberPO::getUserCode, user.getCode()));

        List<GroupDTO> result = new ArrayList<>();
        for (GroupMemberPO ms : memberships) {
            GroupPO group = groupMapper.selectOne(Wrappers.<GroupPO>lambdaQuery().eq(GroupPO::getCode, ms.getGroupCode()));
            if (group != null && group.getStatus() == 1) {
                UserPO owner = userRepository.selectByCode(group.getOwnerCode());
                long count = groupMemberMapper.selectCount(Wrappers.<GroupMemberPO>lambdaQuery().eq(GroupMemberPO::getGroupCode, group.getCode()));
                result.add(toDTO(group, owner != null ? owner.getCode() : "", (int) count));
            }
        }
        return result;
    }

    @Override
    public List<GroupDTO> searchGroups(String keyword) {
        List<GroupPO> groups = groupMapper.selectList(Wrappers.<GroupPO>lambdaQuery()
                .like(GroupPO::getName, keyword)
                .eq(GroupPO::getStatus, 1));
        return groups.stream().map(g -> toDTO(g, "", 0)).collect(Collectors.toList());
    }

    // --- helpers ---
    private UserPO resolveUser(String loginCode) {
        UserPO user = userRepository.selectByCode(loginCode);
        if (user == null) user = userRepository.selectById(loginCode);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    private GroupDTO toDTO(GroupPO g, String ownerCode, int memberCount) {
        GroupDTO dto = new GroupDTO();
        dto.setCode(g.getCode());
        dto.setName(g.getName());
        dto.setAvatar(g.getAvatar());
        dto.setOwnerCode(ownerCode);
        dto.setNotice(g.getNotice());
        dto.setMaxMembers(g.getMaxMembers());
        dto.setMemberCount(memberCount);
        dto.setStatus(g.getStatus());
        return dto;
    }
}
