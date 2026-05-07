package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.FriendDTO;
import com.fish.chat.core.service.FriendService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
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

    @DeleteMapping("/{friendCode}")
    public Result<Void> remove(@PathVariable String friendCode) {
        friendService.removeFriend(friendCode);
        return Result.success("好友已删除", null);
    }

    @GetMapping
    public Result<List<FriendDTO>> list() {
        return Result.success(friendService.listFriends());
    }

    @GetMapping("/search")
    public Result<List<FriendDTO>> search(@RequestParam String keyword) {
        return Result.success(friendService.searchUsers(keyword));
    }
}
