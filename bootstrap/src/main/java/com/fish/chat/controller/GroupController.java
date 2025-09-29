package com.fish.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.entity.Group;
import com.fish.chat.entity.GroupMember;
import com.fish.chat.service.GroupService;
import com.fish.chat.utils.result.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    /**
     * 创建群组
     */
    @PostMapping("/create")
    public Result createGroup(@RequestBody Group group) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long groupId = groupService.createGroup(group, userId);
        return Result.data(groupId);
    }

    /**
     * 解散群组
     */
    @PostMapping("/dismiss/{groupId}")
    public Result dismissGroup(@PathVariable Long groupId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = groupService.dismissGroup(groupId, userId);
        if (result) {
            return Result.ok("群组解散成功");
        } else {
            return Result.error("群组解散失败");
        }
    }

    /**
     * 邀请用户加入群组
     */
    @PostMapping("/invite/{groupId}/{userId}")
    public Result inviteUser(@PathVariable Long groupId, @PathVariable Long userId) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        boolean result = groupService.inviteUserToGroup(groupId, userId, operatorId);
        if (result) {
            return Result.ok("邀请成功");
        } else {
            return Result.error("邀请失败");
        }
    }

    /**
     * 退出群组
     */
    @PostMapping("/leave/{groupId}")
    public Result leaveGroup(@PathVariable Long groupId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = groupService.leaveGroup(groupId, userId);
        if (result) {
            return Result.ok("退出群组成功");
        } else {
            return Result.error("退出群组失败");
        }
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/members/{groupId}")
    public Result getGroupMembers(@PathVariable Long groupId) {
        List<GroupMember> members = groupService.getGroupMembers(groupId);
        return Result.data(members);
    }

    /**
     * 获取用户加入的群组列表
     */
    @GetMapping("/list")
    public Result getUserGroups() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Group> groups = groupService.getUserGroups(userId);
        return Result.data(groups);
    }

    /**
     * 更新群组信息
     */
    @PostMapping("/update/{groupId}")
    public Result updateGroupInfo(@PathVariable Long groupId, @RequestBody Group group) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = groupService.updateGroupInfo(groupId, group, userId);
        if (result) {
            return Result.ok("群组信息更新成功");
        } else {
            return Result.error("群组信息更新失败");
        }
    }

    /**
     * 转让群主
     */
    @PostMapping("/transfer/{groupId}/{newOwnerId}")
    public Result transferOwnership(@PathVariable Long groupId, @PathVariable Long newOwnerId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = groupService.transferOwnership(groupId, newOwnerId, userId);
        if (result) {
            return Result.ok("群主转让成功");
        } else {
            return Result.error("群主转让失败");
        }
    }

    /**
     * 踢出群组成员
     */
    @PostMapping("/kick/{groupId}/{targetUserId}")
    public Result kickMember(@PathVariable Long groupId, @PathVariable Long targetUserId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = groupService.kickMember(groupId, targetUserId, userId);
        if (result) {
            return Result.ok("成员踢出成功");
        } else {
            return Result.error("成员踢出失败");
        }
    }
}