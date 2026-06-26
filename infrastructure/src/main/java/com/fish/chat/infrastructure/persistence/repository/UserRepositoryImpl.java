package com.fish.chat.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fish.chat.domain.user.model.entity.User;
import com.fish.chat.domain.user.repository.UserRepository;
import com.fish.chat.infrastructure.persistence.base.BaseRepositoryImpl;
import com.fish.chat.infrastructure.persistence.converter.BaseConverter;
import com.fish.chat.infrastructure.persistence.converter.UserConverter;
import com.fish.chat.infrastructure.persistence.mapper.UserMapper;
import com.fish.chat.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户仓储实现
 *
 */
@Repository
public class UserRepositoryImpl extends BaseRepositoryImpl<User, UserPO, UserConverter, UserMapper>
        implements UserRepository {
    
    /**
     * ID 回填实现
     */
    @Override
    protected void setIdBackToEntity(User entity, Long id) {
        entity.setId(id);
    }
    
    // ==================== 用户特定业务方法 ====================
    
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = lambdaQuery()
                .eq(UserPO::getUsername, username);
        return findOne(wrapper);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserPO> wrapper = lambdaQuery()
                .eq(UserPO::getUsername, username);
        return exists(wrapper);
    }
    
    @Override
    public List<User> searchByKeyword(String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<UserPO> wrapper = lambdaQuery()
                .like(UserPO::getUsername, keyword)
                .or()
                .like(UserPO::getNickname, keyword);
        return findList(wrapper);
    }
    
    @Override
    public long countByKeyword(String keyword) {
        LambdaQueryWrapper<UserPO> wrapper = lambdaQuery()
                .like(UserPO::getUsername, keyword)
                .or()
                .like(UserPO::getNickname, keyword);
        return count(wrapper);
    }
}
