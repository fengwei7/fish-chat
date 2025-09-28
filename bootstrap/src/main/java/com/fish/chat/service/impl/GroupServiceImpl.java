package com.fish.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fish.chat.entity.Group;
import com.fish.chat.entity.GroupMember;
import com.fish.chat.mapper.mysql.GroupMapper;
import com.fish.chat.mapper.mysql.GroupMemberMapper;
import com.fish.chat.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    
    @Override
    @Transactional
    public Long createGroup(Group group, Long creatorId) {
        // 保存群组信息
        group.setOwnerId(creatorId);
        group.setStatus(1); // 正常状态
        this.save(group);
        
        // 创建群主
        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(creatorId);
        owner.setRole(3); // 群主
        owner.setStatus(1); // 正常状态
        groupMemberMapper.insert(owner);
        
        return group.getId();
    }
    
    @Override
    @Transactional
    public boolean dismissGroup(Long groupId, Long operatorId) {
        Group group = this.getById(groupId);
        if (group == null || !group.getOwnerId().equals(operatorId)) {
            return false; // 只有群主可以解散群组
        }
        
        // 更新群组状态为已解散
        group.setStatus(0);
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        // 更新所有成员状态为已退出
        GroupMember member = new GroupMember();
        member.setStatus(0); // 已退出
        
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        groupMemberMapper.update(member, wrapper);
        
        return true;
    }
    
    @Override
    public boolean inviteUserToGroup(Long groupId, Long userId, Long operatorId) {
        // 检查操作者是否有权限邀请用户
        GroupMember operator = getGroupMember(groupId, operatorId);
        if (operator == null || operator.getStatus() != 1) {
            return false; // 操作者不是群成员或已退出
        }
        
        // 检查群组是否存在且正常
        Group group = this.getById(groupId);
        if (group == null || group.getStatus() != 1) {
            return false;
        }
        
        // 检查群组人数是否已达上限
        List<GroupMember> members = getGroupMembers(groupId);
        if (group.getMaxMembers() != null && members.size() >= group.getMaxMembers()) {
            return false; // 群组人数已达上限
        }
        
        // 检查被邀请用户是否已在群组中
        GroupMember existingMember = getGroupMember(groupId, userId);
        if (existingMember != null) {
            if (existingMember.getStatus() == 1) {
                return false; // 用户已在群组中
            } else {
                // 用户曾经在群组中，更新状态
                existingMember.setStatus(1);
                groupMemberMapper.updateById(existingMember);
                return true;
            }
        }
        
        // 添加新成员
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(1); // 普通成员
        member.setStatus(1); // 正常状态
        groupMemberMapper.insert(member);
        
        return true;
    }
    
    @Override
    public boolean leaveGroup(Long groupId, Long userId) {
        GroupMember member = getGroupMember(groupId, userId);
        if (member == null || member.getStatus() != 1) {
            return false; // 用户不是群成员或已退出
        }
        
        // 检查是否是群主
        Group group = this.getById(groupId);
        if (group.getOwnerId().equals(userId)) {
            return false; // 群主不能直接退出，需要先转让群主或解散群组
        }
        
        // 更新成员状态为已退出
        member.setStatus(0);
        groupMemberMapper.updateById(member);
        
        return true;
    }
    
    @Override
    public List<GroupMember> getGroupMembers(Long groupId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        wrapper.eq("status", 1); // 只查询正常状态的成员
        return groupMemberMapper.selectList(wrapper);
    }
    
    @Override
    public List<Group> getUserGroups(Long userId) {
        // 查询用户加入的群组ID列表
        QueryWrapper<GroupMember> memberWrapper = new QueryWrapper<>();
        memberWrapper.eq("user_id", userId);
        memberWrapper.eq("status", 1);
        List<GroupMember> members = groupMemberMapper.selectList(memberWrapper);
        
        if (members.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询群组信息
        List<Long> groupIds = members.stream().map(GroupMember::getGroupId).collect(Collectors.toList());
        QueryWrapper<Group> groupWrapper = new QueryWrapper<>();
        groupWrapper.in("id", groupIds);
        groupWrapper.eq("status", 1); // 只查询正常状态的群组
        return this.list(groupWrapper);
    }
    
    @Override
    @Transactional
    public boolean updateGroupInfo(Long groupId, Group updatedGroup, Long operatorId) {
        // 检查操作者是否有权限更新群组信息
        GroupMember operator = getGroupMember(groupId, operatorId);
        if (operator == null || (operator.getRole() != 3 && operator.getRole() != 2)) {
            return false; // 只有群主和管理员可以更新群组信息
        }
        
        // 检查群组是否存在且正常
        Group group = this.getById(groupId);
        if (group == null || group.getStatus() != 1) {
            return false;
        }
        
        // 更新群组信息
        group.setName(updatedGroup.getName());
        group.setDescription(updatedGroup.getDescription());
        group.setNotice(updatedGroup.getNotice());
        group.setAvatarUrl(updatedGroup.getAvatarUrl());
        group.setMaxMembers(updatedGroup.getMaxMembers());
        group.setJoinPermission(updatedGroup.getJoinPermission());
        group.setUpdateTime(new Date());
        
        return this.updateById(group);
    }
    
    @Override
    @Transactional
    public boolean transferOwnership(Long groupId, Long newOwnerId, Long operatorId) {
        // 检查操作者是否为群主
        Group group = this.getById(groupId);
        if (group == null || !group.getOwnerId().equals(operatorId)) {
            return false; // 只有群主可以转让群主
        }
        
        // 检查新群主是否为群组成员
        GroupMember newOwner = getGroupMember(groupId, newOwnerId);
        if (newOwner == null || newOwner.getStatus() != 1) {
            return false; // 新群主不是群组成员或已退出
        }
        
        // 转让群主
        group.setOwnerId(newOwnerId);
        group.setUpdateTime(new Date());
        this.updateById(group);
        
        // 更新原群主角色为管理员
        GroupMember oldOwner = getGroupMember(groupId, operatorId);
        oldOwner.setRole(2); // 管理员
        groupMemberMapper.updateById(oldOwner);
        
        // 更新新群主角色为群主
        newOwner.setRole(3); // 群主
        groupMemberMapper.updateById(newOwner);
        
        return true;
    }
    
    @Override
    public boolean kickMember(Long groupId, Long targetUserId, Long operatorId) {
        // 检查操作者是否有权限踢人
        GroupMember operator = getGroupMember(groupId, operatorId);
        if (operator == null || (operator.getRole() != 3 && operator.getRole() != 2)) {
            return false; // 只有群主和管理员可以踢人
        }
        
        // 检查被踢用户是否为群组成员
        GroupMember targetMember = getGroupMember(groupId, targetUserId);
        if (targetMember == null || targetMember.getStatus() != 1) {
            return false; // 被踢用户不是群组成员或已退出
        }
        
        // 检查被踢用户是否为群主
        Group group = this.getById(groupId);
        if (group.getOwnerId().equals(targetUserId)) {
            return false; // 不能踢群主
        }
        
        // 踢出群组
        targetMember.setStatus(0); // 已退出
        groupMemberMapper.updateById(targetMember);
        
        return true;
    }
    
    @Override
    public boolean isGroupMember(Long groupId, Long userId) {
        GroupMember member = getGroupMember(groupId, userId);
        return member != null && member.getStatus() == 1;
    }
    
    /**
     * 获取群组成员
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 群组成员信息
     */
    private GroupMember getGroupMember(Long groupId, Long userId) {
        QueryWrapper<GroupMember> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        wrapper.eq("user_id", userId);
        return groupMemberMapper.selectOne(wrapper);
    }
}