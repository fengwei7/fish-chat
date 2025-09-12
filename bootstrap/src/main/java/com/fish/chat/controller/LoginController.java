package com.fish.chat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fish.chat.dto.UserReqDTO;
import com.fish.chat.service.UserService;
import com.fish.chat.utils.result.Result;
import com.fish.chat.entity.User;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.core.util.RandomUtil;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public Result register(@RequestBody UserReqDTO user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() 
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existingUser = userService.getOne(queryWrapper);

        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 生成随机盐值
        String salt = RandomUtil.randomString(16);
        
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        // 使用SHA256加密并加盐
        newUser.setPassword(DigestUtil.sha256Hex(user.getPassword() + salt));
        newUser.setSalt(salt);
        newUser.setMobile(user.getMobile());
        newUser.setEmail(user.getEmail());
        newUser.setNickname(user.getNickname());
        newUser.setStatus(1);

        boolean saved = userService.save(newUser);
        if (saved) {
            return Result.data("注册成功");
        } else {
            return Result.error("注册失败");
        }
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserReqDTO user) {
        if (user.getUsername() == null || user.getUsername().isEmpty() 
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User dbUser = userService.getOne(queryWrapper);

        // 使用用户对应的盐值验证密码
        if (dbUser == null || !dbUser.getPassword().equals(DigestUtil.sha256Hex(user.getPassword() + dbUser.getSalt()))) {
            return Result.error("用户名或密码错误");
        }

        if (dbUser.getStatus() != 1) {
            return Result.error("账户已被禁用");
        }

        StpUtil.login(dbUser.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("userId", dbUser.getId());
        data.put("username", dbUser.getUsername());
        
        return Result.data(data);
    }

    @PostMapping("/logout")
    public Result logout() {
        StpUtil.logout();
        return Result.data("登出成功");
    }

}