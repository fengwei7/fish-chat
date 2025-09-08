package com.fish.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.dto.UserReqDTO;
import com.fish.chat.service.UserService;
import com.fish.chat.utils.result.Result;
import com.fish.chat.entity.User;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody UserReqDTO user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() 
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User dbUser = userService.getOne(queryWrapper);

        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        StpUtil.login(dbUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("userId", dbUser.getId());
        data.put("username", dbUser.getUsername());
        
        return Result.data(data);
    }

}