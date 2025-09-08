package com.fish.chat.controller;

import com.fish.chat.service.UserService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author fish-chat
 * @since 2025-09-06
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;

}
