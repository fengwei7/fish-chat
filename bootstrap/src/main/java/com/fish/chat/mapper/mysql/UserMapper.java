package com.fish.chat.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fish.chat.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 功能测试
    User getMessage(User user);
}
