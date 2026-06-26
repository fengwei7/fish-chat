package com.fish.chat.infrastructure.persistence.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fish.chat.common.entity.BasePO;

/**
 * 基础 Mapper 接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，获得基础 CRUD 能力
 * 
 * 设计说明：
 * - 所有业务 Mapper 继承此接口
 * - 可在此定义通用的自定义 SQL 方法
 * - 具体业务方法在各自的 Mapper 中定义
 * 
 * @param <P> PO 类型（Persistent Object）
 */
public interface MyBaseMapper<P extends BasePO> extends BaseMapper<P> {

}
