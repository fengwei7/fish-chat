package com.fish.chat.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fish.chat.core.entity.po.ChannelMemberPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChannelMemberMapper extends BaseMapper<ChannelMemberPO> {}