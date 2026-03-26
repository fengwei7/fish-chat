package com.fish.chat.core.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fish.chat.core.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
}
