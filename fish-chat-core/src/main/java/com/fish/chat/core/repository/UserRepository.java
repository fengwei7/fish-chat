package com.fish.chat.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fish.chat.common.repository.BaseRepository;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 用户数据访问层
 */
@Repository
public class UserRepository extends BaseRepository<UserPO> {

    @Resource
    private UserMapper userMapper;

    @Override
    protected BaseMapper<UserPO> getBaseMapper() {
        return userMapper;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    public UserPO findByUsername(String username) {
        return userMapper.selectOne(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getUsername, username));
    }
}
