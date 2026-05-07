package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.ChannelDTO;
import com.fish.chat.core.service.ChannelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/channels")
@CrossOrigin
public class ChannelController {

    @Resource
    private ChannelService channelService;

    @PostMapping
    public Result<ChannelDTO> create(@RequestBody Map<String, String> req) {
        return Result.success(channelService.createChannel(
                req.get("name"), req.get("avatar"), req.get("description")));
    }

    @GetMapping("/{code}")
    public Result<ChannelDTO> get(@PathVariable String code) {
        return Result.success(channelService.getChannel(code));
    }

    @PostMapping("/{code}/subscribe")
    public Result<Void> subscribe(@PathVariable String code) {
        channelService.subscribe(code);
        return Result.success("订阅成功", null);
    }

    @DeleteMapping("/{code}/subscribe")
    public Result<Void> unsubscribe(@PathVariable String code) {
        channelService.unsubscribe(code);
        return Result.success("取消订阅", null);
    }

    @GetMapping("/my")
    public Result<List<ChannelDTO>> listMy() {
        return Result.success(channelService.listMyChannels());
    }

    @GetMapping("/search")
    public Result<List<ChannelDTO>> search(@RequestParam String keyword) {
        return Result.success(channelService.searchChannels(keyword));
    }
}
