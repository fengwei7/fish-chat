package com.fish.chat.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fish.chat.core.entity.po.ChannelPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChannelMapper extends BaseMapper<ChannelPO> {}