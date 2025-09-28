package com.fish.chat.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fish.chat.entity.Group;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {
}