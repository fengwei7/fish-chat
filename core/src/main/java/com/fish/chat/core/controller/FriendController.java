package com.fish.chat.core.controller;

import com.fish.chat.common.result.PageResult;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.FriendDTO;
import com.fish.chat.core.service.FriendService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/friends")
@CrossOrigin
public class FriendController {

    @Resource
    private FriendService friendService;

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, String> req) {
        friendService.addFriend(req.get("friendCode"), req.get("remark"));
        return Result.success("好友请求已发送", null);
    }

    @PostMapping("/accept")
    public Result<Void> accept(@RequestBody Map<String, String> req) {
        friendService.acceptFriend(req.get("friendCode"));
        return Result.success("已添加好友", null);
    }

    @PostMapping("/remove")
    public Result<Void> remove(@RequestBody Map<String, String> req) {
        friendService.removeFriend(req.get("friendCode"));
        return Result.success("好友已删除", null);
    }

    @GetMapping
    public Result<PageResult<FriendDTO>> list(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(friendService.listFriends(pageNum, pageSize));
    }

    @GetMapping("/requests")
    public Result<PageResult<FriendDTO>> requests(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(friendService.listFriendRequests(pageNum, pageSize));
    }

    @GetMapping("/search")
    public Result<PageResult<FriendDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(friendService.searchUsers(keyword, pageNum, pageSize));
    }
}
