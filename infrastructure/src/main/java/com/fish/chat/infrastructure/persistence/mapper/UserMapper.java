package com.fish.chat.infrastructure.persistence.mapper;

import com.fish.chat.infrastructure.persistence.base.MyBaseMapper;
import com.fish.chat.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends MyBaseMapper<UserPO> {
}
