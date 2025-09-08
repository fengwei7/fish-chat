package com.fish.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.dto.UserReqDTO;
import com.fish.chat.service.UserService;
import com.fish.chat.utils.result.Result;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;

    @PostMapping("/login")
    public Result login(UserReqDTO user) {
        StpUtil.login(user.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("token", StpUtil.getTokenValue());
        return Result.data(map);
    }

}
