package com.fish.chat.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fish.chat.common.repository.BaseRepository;
import com.fish.chat.core.entity.po.UserPO;
import com.fish.chat.core.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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
    public UserPO selectByUsername(String username) {
        return userMapper.selectOne(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getUsername, username));
    }

    /**
     * 根据用户code查询用户
     *
     * @param code 用户code
     * @return 用户实体
     */
    public UserPO selectByCode(String code) {
        return userMapper.selectOne(Wrappers.<UserPO>lambdaQuery().eq(UserPO::getCode, code));
    }

    /**
     * 更新用户信息
     *
     * @param userPO 用户实体
     * @return 是否更新成功
     */
    public boolean updateById(UserPO userPO) {
        return userMapper.updateById(userPO) > 0;
    }

    /**
     * 模糊搜索用户（用户名或昵称）
     */
    public List<UserPO> searchByKeyword(String keyword) {
        return userMapper.selectList(Wrappers.<UserPO>lambdaQuery()
                .like(UserPO::getUsername, keyword)
                .or()
                .like(UserPO::getNickname, keyword)
                .last("LIMIT 20"));
    }
}
