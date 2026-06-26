package com.fish.chat.infrastructure.persistence.converter;

import com.fish.chat.domain.user.model.entity.User;
import com.fish.chat.infrastructure.persistence.po.UserPO;
import org.mapstruct.Mapper;

/**
 * 用户转换器（MapStruct 实现）
 * 
 * 设计原则：
 * - 使用 MapStruct 编译期生成代码，性能优于手写
 * - 纯转换逻辑，不包含业务规则
 * - 空值安全：输入 null 时返回 null
 * 
 * 使用说明：
 * - 编译时自动生成实现类 UserConverterImpl
 * - 无需手写 getter/setter 调用代码
 */
@Mapper(componentModel = "spring")
public interface UserConverter extends BaseConverter<User, UserPO> {
    
    @Override
    User toEntity(UserPO p);
    
    @Override
    UserPO toPO(User e);
}