package com.fish.chat.controller;

import com.fish.chat.service.UserService;
import com.fish.chat.utils.result.Result;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author fish-chat
 * @since 2025-09-06
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("/getInfoById/{id}")
    public Result getInfoById(@PathVariable Long id) {
        return Result.data(userService.getById(id));
    }

}
