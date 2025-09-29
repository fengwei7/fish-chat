package com.fish.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fish.chat.entity.Group;
import com.fish.chat.entity.GroupMember;
import java.util.List;

public interface GroupService extends IService<Group> {

    /**
     * 创建群组
     *
     * @param group 群组信息
     * @param creatorId 创建者ID
     * @return 群组ID
     */
    Long createGroup(Group group, Long creatorId);

    /**
     * 解散群组
     *
     * @param groupId 群组ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean dismissGroup(Long groupId, Long operatorId);

    /**
     * 邀请用户加入群组
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean inviteUserToGroup(Long groupId, Long userId, Long operatorId);

    /**
     * 用户退出群组
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveGroup(Long groupId, Long userId);

    /**
     * 获取群组成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<GroupMember> getGroupMembers(Long groupId);

    /**
     * 获取用户加入的群组列表
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<Group> getUserGroups(Long userId);

    /**
     * 更新群组信息
     *
     * @param groupId 群组ID
     * @param group 更新的群组信息
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean updateGroupInfo(Long groupId, Group group, Long operatorId);

    /**
     * 转让群主
     *
     * @param groupId 群组ID
     * @param newOwnerId 新群主ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean transferOwnership(Long groupId, Long newOwnerId, Long operatorId);

    /**
     * 踢出群组成员
     *
     * @param groupId 群组ID
     * @param targetUserId 被踢用户ID
     * @param operatorId 操作者ID
     * @return 是否成功
     */
    boolean kickMember(Long groupId, Long targetUserId, Long operatorId);

    /**
     * 检查用户是否为群组成员
     *
     * @param groupId 群组ID
     * @param userId 用户ID
     * @return 是否为成员
     */
    boolean isGroupMember(Long groupId, Long userId);
}