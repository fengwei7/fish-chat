package com.fish.chat.core.controller;

import com.fish.chat.common.constants.UrlConstants;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.common.result.Result;
import com.fish.chat.core.entity.dto.ChannelDTO;
import com.fish.chat.core.entity.req.SetChannelAdminRequest;
import com.fish.chat.core.entity.req.TransferChannelRequest;
import com.fish.chat.core.service.ChannelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(UrlConstants.HTTP_URL_PREFIX + "/channels")
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
        return Result.success();
    }

    @PostMapping("/{code}/unsubscribe")
    public Result<Void> unsubscribe(@PathVariable String code) {
        channelService.unsubscribe(code);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<ChannelDTO>> listMy(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(channelService.listMyChannels(pageNum, pageSize));
    }

    @GetMapping("/search")
    public Result<PageResult<ChannelDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(channelService.searchChannels(keyword, pageNum, pageSize));
    }

    /**
     * 转让频道（仅创建者可操作）
     */
    @PostMapping("/{code}/transfer")
    public Result<Void> transfer(@PathVariable String code, 
                                  @Valid @RequestBody TransferChannelRequest req) {
        channelService.transferChannel(code, req.getNewOwnerCode());
        return Result.success();
    }

    /**
     * 设置/取消管理员（仅创建者可操作）
     */
    @PostMapping("/{code}/admin/{userCode}")
    public Result<Void> setAdmin(@PathVariable String code, 
                                 @PathVariable String userCode,
                                 @Valid @RequestBody SetChannelAdminRequest req) {
        channelService.setAdmin(code, userCode, req.getIsAdmin());
        return Result.success();
    }
}
