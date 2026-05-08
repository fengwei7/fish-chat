package com.fish.chat.core.controller;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.GroupDTO;
import com.fish.chat.core.entity.req.AddMemberRequest;
import com.fish.chat.core.entity.req.CreateGroupRequest;
import com.fish.chat.core.service.GroupService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

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

    @PostMapping("/{code}/dismiss")
    public Result<Void> dismiss(@PathVariable String code) {
        groupService.dismissGroup(code);
        return Result.success("群组已解散", null);
    }

    @PostMapping("/{code}/members")
    public Result<Void> addMember(@PathVariable String code, @Valid @RequestBody AddMemberRequest req) {
        groupService.addMember(code, req.getUserCode());
        return Result.success("成员已添加", null);
    }

    @PostMapping("/{code}/members/remove")
    public Result<Void> removeMember(@PathVariable String code, @RequestBody Map<String, String> req) {
        groupService.removeMember(code, req.get("userCode"));
        return Result.success("成员已移除", null);
    }

    @GetMapping("/my")
    public Result<PageResult<GroupDTO>> listMy(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(groupService.listMyGroups(pageNum, pageSize));
    }

    @GetMapping("/search")
    public Result<PageResult<GroupDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(groupService.searchGroups(keyword, pageNum, pageSize));
    }
}
