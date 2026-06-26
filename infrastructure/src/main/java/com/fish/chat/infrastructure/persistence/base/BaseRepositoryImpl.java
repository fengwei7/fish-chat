package com.fish.chat.infrastructure.persistence.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fish.chat.common.entity.BasePO;
import com.fish.chat.common.exception.BusinessException;
import com.fish.chat.common.result.PageResult;
import com.fish.chat.domain.base.BaseEntity;
import com.fish.chat.domain.base.BaseRepository;
import com.fish.chat.infrastructure.persistence.converter.BaseConverter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 基础仓储实现类
 *
 */
@Slf4j
public abstract class BaseRepositoryImpl<E extends BaseEntity, P extends BasePO, C extends BaseConverter<E, P>, M extends MyBaseMapper<P>>
        implements BaseRepository<E> {
    
    @Resource
    protected M baseMapper;
    
    @Resource
    protected C baseConverter;
    
    // ==================== 基础 CRUD ====================
    
    @Override
    public E findById(Long id) {
        if (id == null) {
            return null;
        }
        P po = baseMapper.selectById(id);
        return baseConverter.toEntity(po);
    }
    
    @Override
    public E findByIdOrThrow(Long id, String errorMessage) {
        E entity = findById(id);
        if (entity == null) {
            throw new BusinessException(errorMessage != null ? errorMessage : "数据不存在，ID: " + id);
        }
        return entity;
    }
    
    @Override
    public E save(E entity) {
        P po = baseConverter.toPO(entity);
        if (po.getId() == null) {
            // 新增
            baseMapper.insert(po);
            // 回填 ID
            setIdBackToEntity(entity, po.getId());
            log.debug("新增数据成功，ID: {}", po.getId());
        } else {
            // 更新
            baseMapper.updateById(po);
            log.debug("更新数据成功，ID: {}", po.getId());
        }
        return entity;
    }
    
    @Override
    public List<E> saveBatch(List<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return entities;
        }
        for (E entity : entities) {
            save(entity);
        }
        return entities;
    }
    
    @Override
    public void delete(Long id) {
        if (id != null) {
            baseMapper.deleteById(id);
            log.debug("删除数据成功，ID: {}", id);
        }
    }
    
    @Override
    public int delete(Wrapper<?> wrapper) {
        @SuppressWarnings("unchecked")
        Wrapper<P> typedWrapper = (Wrapper<P>) wrapper;
        return baseMapper.delete(typedWrapper);
    }
    
    // ==================== 条件查询 ====================
    
    @Override
    public E findByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<P> wrapper = new LambdaQueryWrapper<P>()
                .eq(P::getCode, code);
        P po = baseMapper.selectOne(wrapper);
        return baseConverter.toEntity(po);
    }
    
    @Override
    public E selectByMap(Map<String, Object> columnMap) {
        List<P> poList = baseMapper.selectByMap(columnMap);
        if (poList == null || poList.isEmpty()) {
            return null;
        }
        return baseConverter.toEntity(poList.get(0));
    }
    
    @Override
    public List<E> listByMap(Map<String, Object> columnMap) {
        List<P> poList = baseMapper.selectByMap(columnMap);
        return baseConverter.toEntity(poList);
    }
    
    @Override
    public boolean exists(Wrapper<?> wrapper) {
        @SuppressWarnings("unchecked")
        Wrapper<P> typedWrapper = (Wrapper<P>) wrapper;
        Long count = baseMapper.selectCount(typedWrapper);
        return count != null && count > 0;
    }
    
    // ==================== 分页查询 ====================
    
    @Override
    public PageResult<E> findPage(int pageNum, int pageSize, Wrapper<?> wrapper) {
        @SuppressWarnings("unchecked")
        Wrapper<P> typedWrapper = (Wrapper<P>) wrapper;
        
        Page<P> page = new Page<>(pageNum, pageSize);
        Page<P> poPage = baseMapper.selectPage(page, typedWrapper);
        
        // 使用 common.PageResult 统一分页格式
        return PageResult.of(
                baseConverter.toEntity(poPage.getRecords()),
                poPage.getCurrent(),
                poPage.getSize(),
                poPage.getTotal()
        );
    }
    
    @Override
    public List<E> findList(Wrapper<?> wrapper) {
        @SuppressWarnings("unchecked")
        Wrapper<P> typedWrapper = (Wrapper<P>) wrapper;
        List<P> poList = baseMapper.selectList(typedWrapper);
        return baseConverter.toEntity(poList);
    }
    
    @Override
    public E findOne(Wrapper<?> wrapper) {
        @SuppressWarnings("unchecked")
        Wrapper<P> typedWrapper = (Wrapper<P>) wrapper;
        P po = baseMapper.selectOne(typedWrapper);
        return baseConverter.toEntity(po);
    }
    
    @Override
    public long count(Wrapper<?> wrapper) {
        @SuppressWarnings("unchecked")
        Wrapper<P> typedWrapper = (Wrapper<P>) wrapper;
        Long count = baseMapper.selectCount(typedWrapper);
        return count != null ? count : 0;
    }

    // ==================== 扩展点（子类可重写）====================

    /**
     * ID 回填到 Entity
     *
     * 子类可以重写此方法实现自定义回填逻辑
     * 默认实现：无操作（如果 Entity 的 setId 方法可用）
     *
     * 使用示例：
     * <pre>
     * {@code
     * @Override
     * protected void setIdBackToEntity(User entity, Long id) {
     *     entity.setId(id);
     * }
     * }
     * </pre>
     *
     * @param entity 领域实体
     * @param id 生成的 ID
     */
    protected void setIdBackToEntity(E entity, Long id) {
        // 默认空实现，子类可重写
        // 例如：((User) entity).setId(id);
    }
    /**
     * 构建 LambdaQueryWrapper
     *
     * 子类可使用此方法快速构建查询条件
     *
     * @return LambdaQueryWrapper 实例
     */
    protected LambdaQueryWrapper<P> lambdaQuery() {
        return new LambdaQueryWrapper<>();
    }
}
