package com.fish.chat.domain.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fish.chat.common.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 基础仓储接口
 * 
 * 定义仓储的标准操作规范，所有业务仓储继承此接口
 * 
 * 设计原则：
 * - 依赖倒置：domain层定义接口，infrastructure层实现
 * - 泛型编程：通过泛型保证类型安全
 * - 职责单一：只定义数据访问操作，不包含业务逻辑
 * 
 * @param <E> 领域实体类型（Entity）
 */
public interface BaseRepository<E> {
    
    // ==================== 基础 CRUD ====================
    
    /**
     * 根据 ID 查询
     * 
     * @param id 实体 ID
     * @return 领域实体，不存在时返回 null
     */
    E findById(Long id);
    
    /**
     * 根据 ID 查询（不存在则抛异常）
     * 
     * @param id 实体 ID
     * @param errorMessage 异常信息
     * @return 领域实体
     * @throws RuntimeException 当实体不存在时
     */
    E findByIdOrThrow(Long id, String errorMessage);
    
    /**
     * 保存实体（新增或更新）
     * - ID 为空：新增
     * - ID 不为空：更新
     * 
     * @param entity 领域实体
     * @return 保存后的实体（包含回填的 ID）
     */
    E save(E entity);
    
    /**
     * 批量保存实体
     * 
     * @param entities 领域实体列表
     * @return 保存后的实体列表
     */
    List<E> saveBatch(List<E> entities);
    
    /**
     * 删除实体
     * 
     * @param id 实体 ID
     */
    void delete(Long id);
    
    /**
     * 根据条件删除
     * 
     * @param wrapper 查询条件
     * @return 删除的记录数
     */
    int delete(Wrapper<?> wrapper);
    
    // ==================== 条件查询 ====================
    
    /**
     * 根据 code 查询
     * 
     * @param code 业务编码
     * @return 领域实体，不存在时返回 null
     */
    E findByCode(String code);
    
    /**
     * 根据 Map 条件查询单条
     * 
     * @param columnMap 字段名-值映射
     * @return 领域实体，不存在时返回 null
     */
    E selectByMap(Map<String, Object> columnMap);
    
    /**
     * 根据 Map 条件查询列表
     * 
     * @param columnMap 字段名-值映射
     * @return 领域实体列表
     */
    List<E> listByMap(Map<String, Object> columnMap);
    
    /**
     * 检查是否存在
     * 
     * @param wrapper 查询条件
     * @return true-存在，false-不存在
     */
    boolean exists(Wrapper<?> wrapper);
    
    // ==================== 分页查询 ====================
    
    /**
     * 分页查询
     * 
     * @param pageNum 页码（从 0 开始，与 common.PageResult 保持一致）
     * @param pageSize 每页大小
     * @param wrapper 查询条件
     * @return 分页结果
     */
    PageResult<E> findPage(int pageNum, int pageSize, Wrapper<?> wrapper);
    
    /**
     * 查询列表
     * 
     * @param wrapper 查询条件
     * @return 领域实体列表
     */
    List<E> findList(Wrapper<?> wrapper);
    
    /**
     * 查询单条
     * 
     * @param wrapper 查询条件
     * @return 领域实体，不存在时返回 null
     */
    E findOne(Wrapper<?> wrapper);
    
    /**
     * 查询总数
     * 
     * @param wrapper 查询条件
     * @return 记录数
     */
    long count(Wrapper<?> wrapper);
}
