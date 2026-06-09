package com.fish.chat.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.core.netty.chat.room.RoomManager;
import com.fish.chat.core.enums.CommonStatus;
import com.fish.chat.core.enums.MemberRole;
import com.fish.chat.core.entity.dto.GroupDTO;
import com.fish.chat.core.entity.dto.GroupMemberDTO;
import com.fish.chat.core.netty.chat.SessionManager;
import com.fish.chat.core.entity.po.GroupMemberPO;
import com.fish.chat.core.entity.po.GroupPO;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.repository.GroupRepository;
import com.fish.chat.core.repository.UserRepository;
import com.fish.chat.core.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    @Resource private GroupRepository groupRepository;
    @Resource private UserRepository userRepository;
    @Resource private RoomManager roomManager;
    @Resource private SessionManager sessionManager;

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
        group.setStatus(CommonStatus.NORMAL.getValue());
        groupRepository.save(group);
        // BasePO auto-fill sets code

        // 创建者自动加入
        GroupMemberPO member = new GroupMemberPO();
        member.setGroupCode(group.getCode());
        member.setUserCode(owner.getCode());
        member.setRole(MemberRole.OWNER.getValue());
        groupRepository.insertMember(member);

        // 加载到 RoomManager
        java.util.Set<String> members = java.util.Collections.singleton(owner.getCode());
        roomManager.getOrCreateGroupRoom(group.getCode(), group.getName(), group.getAvatar(), members);

        return toDTO(group, owner.getCode(), 1);
    }

    @Override
    public GroupDTO getGroup(String code) {
        GroupPO group = groupRepository.selectByCode(code);
        if (group == null) throw new BusinessException("群组不存在");

        UserPO owner = userRepository.selectByCode(group.getOwnerCode());
        long count = groupRepository.countMembers(group.getCode());

        return toDTO(group, owner != null ? owner.getCode() : "", (int) count);
    }

    @Transactional
    @Override
    public void dismissGroup(String code) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO current = resolveUser(userCode);

        GroupPO group = groupRepository.selectByCode(code);
        if (group == null) throw new BusinessException("群组不存在");
        if (!group.getOwnerCode().equals(current.getCode())) throw new BusinessException("只有群主可以解散群组");

        group.setStatus(CommonStatus.DISABLED.getValue());
        groupRepository.updateById(group);
    }

    @Transactional
    @Override
    public void addMember(String groupCode, String userCode) {
        GroupPO group = groupRepository.selectByCode(groupCode);
        if (group == null) throw new BusinessException("群组不存在");

        UserPO user = userRepository.selectByCode(userCode);
        if (user == null) throw new BusinessException("用户不存在");

        // 检查是否已加入
        if (groupRepository.isMember(group.getCode(), user.getCode())) {
            throw new BusinessException("用户已在群中");
        }

        GroupMemberPO member = new GroupMemberPO();
        member.setGroupCode(group.getCode());
        member.setUserCode(user.getCode());
        member.setRole(MemberRole.MEMBER.getValue());
        groupRepository.insertMember(member);

        // 更新 Room
        roomManager.addMemberToGroup(groupCode, userCode);
    }

    @Transactional
    @Override
    public void removeMember(String groupCode, String userCode) {
        GroupPO group = groupRepository.selectByCode(groupCode);
        if (group == null) throw new BusinessException("群组不存在");

        UserPO user = userRepository.selectByCode(userCode);
        if (user == null) throw new BusinessException("用户不存在");

        groupRepository.deleteMember(group.getCode(), user.getCode());

        roomManager.removeMemberFromGroup(groupCode, userCode);
    }

    @Override
    public PageResult<GroupDTO> listMyGroups(int pageNum, int pageSize) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO user = resolveUser(userCode);

        Page<GroupMemberPO> memberPage = groupRepository.selectMemberPage(
                user.getCode(), new Page<>(pageNum, pageSize));

        List<String> groupCodes = memberPage.getRecords().stream()
                .map(GroupMemberPO::getGroupCode)
                .collect(Collectors.toList());

        if (groupCodes.isEmpty()) {
            return PageResult.of(new ArrayList<>(), pageNum, pageSize, memberPage.getTotal());
        }

        List<GroupPO> groups = groupRepository.selectByCodes(groupCodes);
        Map<String, GroupPO> groupMap = groups.stream()
                .collect(Collectors.toMap(GroupPO::getCode, g -> g));

        List<GroupDTO> result = new ArrayList<>();
        for (GroupMemberPO member : memberPage.getRecords()) {
            GroupPO g = groupMap.get(member.getGroupCode());
            if (g != null) {
                UserPO owner = userRepository.selectByCode(g.getOwnerCode());
                long count = groupRepository.countMembers(g.getCode());
                result.add(toDTO(g, owner != null ? owner.getCode() : "", (int) count));
            }
        }
        return PageResult.of(result, pageNum, pageSize, memberPage.getTotal());
    }

    @Transactional
    @Override
    public void leaveGroup(String groupCode) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO current = resolveUser(userCode);

        GroupPO group = groupRepository.selectByCode(groupCode);
        if (group == null) throw new BusinessException("群组不存在");
        
        // 检查用户是否是群主
        if (group.getOwnerCode().equals(current.getCode())) {
            throw new BusinessException("群主不能退出群组，请先转让或解散");
        }

        // 检查用户是否在群组中
        if (!groupRepository.isMember(group.getCode(), current.getCode())) {
            throw new BusinessException("您不在该群组中");
        }

        // 移除成员
        removeMember(groupCode, current.getCode());
    }

    @Override
    public PageResult<GroupDTO> searchGroups(String keyword, int pageNum, int pageSize) {
        Page<GroupPO> pageParam = new Page<>(pageNum, pageSize);
        Page<GroupPO> pageResult = groupRepository.selectPage(pageParam, Wrappers.<GroupPO>lambdaQuery()
                .like(GroupPO::getName, keyword)
                .eq(GroupPO::getStatus, CommonStatus.NORMAL.getValue()));
        List<GroupDTO> list = pageResult.getRecords().stream()
                .map(g -> toDTO(g, "", 0)).collect(Collectors.toList());
        return PageResult.of(list, pageNum, pageSize, pageResult.getTotal());
    }

    @Override
    public PageResult<GroupMemberDTO> listGroupMembers(String groupCode, int pageNum, int pageSize) {
        // 检查群组是否存在
        GroupPO group = groupRepository.selectByCode(groupCode);
        if (group == null) throw new BusinessException("群组不存在");

        // 分页查询群成员
        Page<GroupMemberPO> memberPage = groupRepository.selectGroupMemberPage(
                groupCode, new Page<>(pageNum, pageSize));

        List<String> userCodes = memberPage.getRecords().stream()
                .map(GroupMemberPO::getUserCode)
                .collect(Collectors.toList());

        if (userCodes.isEmpty()) {
            return PageResult.of(new ArrayList<>(), pageNum, pageSize, memberPage.getTotal());
        }

        // 批量查询用户信息
        List<UserPO> users = userRepository.selectByCodes(userCodes);
        Map<String, UserPO> userMap = users.stream()
                .collect(Collectors.toMap(UserPO::getCode, u -> u));

        // 组装 DTO
        List<GroupMemberDTO> result = new ArrayList<>();
        for (GroupMemberPO member : memberPage.getRecords()) {
            UserPO user = userMap.get(member.getUserCode());
            if (user != null) {
                GroupMemberDTO dto = new GroupMemberDTO();
                dto.setCode(user.getCode());
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatarUrl(user.getAvatarUrl());
                dto.setRole(member.getRole());
                dto.setOnline(sessionManager.isOnline(user.getCode()));
                result.add(dto);
            }
        }
        return PageResult.of(result, pageNum, pageSize, memberPage.getTotal());
    }

    @Transactional
    @Override
    public GroupDTO updateGroup(String code, String name, String avatar, String notice) {
        String userCode = StpUtil.getLoginIdAsString();
        UserPO current = resolveUser(userCode);

        GroupPO group = groupRepository.selectByCode(code);
        if (group == null) throw new BusinessException("群组不存在");

        // 权限检查：仅群主或管理员可以修改群组信息
        GroupMemberPO member = groupRepository.selectMember(group.getCode(), current.getCode());
        if (member == null) {
            throw new BusinessException("您不在该群组中");
        }
        
        MemberRole role = MemberRole.fromValue(member.getRole());
        if (!role.canManage()) {
            throw new BusinessException("只有群主或管理员可以修改群组信息");
        }

        // 选择性更新非空字段
        if (name != null) {
            group.setName(name);
        }
        if (avatar != null) {
            group.setAvatar(avatar);
        }
        if (notice != null) {
            group.setNotice(notice);
        }

        groupRepository.updateById(group);

        // 更新 RoomManager 中的群组信息
        if (name != null || avatar != null) {
            roomManager.updateGroupInfo(group.getCode(), group.getName(), group.getAvatar());
        }

        UserPO owner = userRepository.selectByCode(group.getOwnerCode());
        long count = groupRepository.countMembers(group.getCode());
        return toDTO(group, owner != null ? owner.getCode() : "", (int) count);
    }

    @Transactional
    @Override
    public void setGroupAdmin(String groupCode, String userCode, boolean isAdmin) {
        String currentUserCode = StpUtil.getLoginIdAsString();
        UserPO current = resolveUser(currentUserCode);

        GroupPO group = groupRepository.selectByCode(groupCode);
        if (group == null) throw new BusinessException("群组不存在");

        // 权限检查：仅群主可以设置管理员
        if (!group.getOwnerCode().equals(currentUserCode)) {
            throw new BusinessException("仅群主可设置管理员");
        }

        // 不能设置群主自己为管理员（已经是群主）
        if (group.getOwnerCode().equals(userCode)) {
            throw new BusinessException("不能设置群主为管理员");
        }

        // 查询目标用户是否为群成员
        GroupMemberPO member = groupRepository.selectMember(groupCode, userCode);
        if (member == null) {
            throw new BusinessException("该用户不在群组中");
        }

        // 更新角色
        if (isAdmin) {
            member.setRole(MemberRole.ADMIN.getValue());
        } else {
            member.setRole(MemberRole.MEMBER.getValue());
        }
        groupRepository.updateMember(member);
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
