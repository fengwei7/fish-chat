package com.fish.chat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.dto.UserDTO;
import com.fish.chat.dto.UserReqDTO;
import com.fish.chat.entity.User;
import com.fish.chat.service.UserService;
import com.fish.chat.utils.result.Result;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author fish-chat
 * @since 2025-09-06
 */
@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("/info")
    public Result getInfoById() {
        Long id = StpUtil.getLoginIdAsLong();
        User user = userService.getById(id);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return Result.data(userDTO);
    }

    @PostMapping("/update")
    public Result updateInfo(@RequestBody UserReqDTO user) {
        Long id = StpUtil.getLoginIdAsLong();
        if (user.getId() == null || user.getId() == 0) {
            return Result.error("用户ID不能为空");
        }
        if (!user.getId().equals(id)) {
            return Result.error("用户ID不匹配");
        }
        User dbUser = new User();
        BeanUtils.copyProperties(user, dbUser);

        if (!userService.updateById(dbUser)){
            return Result.error("更新失败");
        }
        return Result.ok();
    }

}