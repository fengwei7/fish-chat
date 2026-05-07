package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.GroupDTO;
import com.fish.chat.core.entity.req.AddMemberRequest;
import com.fish.chat.core.entity.req.CreateGroupRequest;
import com.fish.chat.core.service.GroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/groups")
@CrossOrigin
public class GroupController {

    @Resource
    private GroupService groupService;

    @PostMapping
    public Result<GroupDTO> create(@Valid @RequestBody CreateGroupRequest req) {
        return Result.success(groupService.createGroup(req.getName(), req.getAvatar()));
    }

    @GetMapping("/{code}")
    public Result<GroupDTO> get(@PathVariable String code) {
        return Result.success(groupService.getGroup(code));
    }

    @DeleteMapping("/{code}")
    public Result<Void> dismiss(@PathVariable String code) {
        groupService.dismissGroup(code);
        return Result.success("群组已解散", null);
    }

    @PostMapping("/{code}/members")
    public Result<Void> addMember(@PathVariable String code, @Valid @RequestBody AddMemberRequest req) {
        groupService.addMember(code, req.getUserCode());
        return Result.success("成员已添加", null);
    }

    @DeleteMapping("/{code}/members/{userCode}")
    public Result<Void> removeMember(@PathVariable String code, @PathVariable String userCode) {
        groupService.removeMember(code, userCode);
        return Result.success("成员已移除", null);
    }

    @GetMapping("/my")
    public Result<List<GroupDTO>> listMy() {
        return Result.success(groupService.listMyGroups());
    }

    @GetMapping("/search")
    public Result<List<GroupDTO>> search(@RequestParam String keyword) {
        return Result.success(groupService.searchGroups(keyword));
    }
}
