package com.fish.chat.common.repository;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.fish.chat.common.entity.BasePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BaseRepository 抽象实现类
 *
 * @param <M> 实体类型，必须继承 BasePO
 */
public abstract class BaseRepository<M extends BasePO> {
    

    /**
     * 获取基础 Mapper，由子类实现注入具体的 Mapper
     *
     * @return BaseMapper
     */
    protected abstract BaseMapper<M> getBaseMapper();

    
    @Transactional(rollbackFor = Exception.class)
    public M save(M data) {
        getBaseMapper().insert(data);
        return data;
    }

    
    @Transactional(rollbackFor = Exception.class)
    public List<M> save(List<M> dataList) {
        for (M data : dataList) {
            save(data);
        }
        return dataList;
    }

    
    public M selectById(String id) {
        return getBaseMapper().selectById(id);
    }

    
    public List<M> selectByIds(List<String> ids) {
        return getBaseMapper().selectBatchIds(ids);
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        getBaseMapper().deleteById(id);
    }

    
    @Transactional(rollbackFor = Exception.class)
    public int deleteAbsoluteByIds(List<M> datas) {
        int count = 0;
        for (M data : datas) {
            count += getBaseMapper().deleteById(data.getId());
        }
        return count;
    }

    
    @Transactional(rollbackFor = Exception.class)
    public Long save(M data, Map<String, Object> foreignMap) {
        if (foreignMap != null && !foreignMap.isEmpty()) {
            foreignMap.forEach((key, value) -> setFieldValue(data, key, value));
        }
        getBaseMapper().insert(data);
        return data.getId();
    }

    
    @Transactional(rollbackFor = Exception.class)
    public Long save(M data, String parentId) {
        setFieldValue(data, "parentId", parentId);
        getBaseMapper().insert(data);
        return data.getId();
    }

    
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveBatchByParentId(String parentId, List<M> entityList) {
        return entityList.stream()
                .map(entity -> save(entity, parentId))
                .collect(Collectors.toList());
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void update(M data, Map<String, Object> columnMap) {
        QueryWrapper<M> wrapper = new QueryWrapper<>();
        if (columnMap != null && !columnMap.isEmpty()) {
            columnMap.forEach(wrapper::eq);
        }
        getBaseMapper().update(data, wrapper);
    }

    
    @Transactional(rollbackFor = Exception.class)
    public int delete(String id) {
        return getBaseMapper().deleteById(id);
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void delete(M data) {
        getBaseMapper().deleteById(data.getId());
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void restoreDeleted(M data) {
        setFieldValue(data, "deleted", 0);
        
        getBaseMapper().updateById(data);
    }

    
    public M selectOne(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap) {
        QueryWrapper<M> wrapper = buildWrapper(columnMap);
        return getBaseMapper().selectOne(wrapper);
    }

    
    public M selectFullOne(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap) {
        return selectOne(columnMap);
    }

    
    public List<M> selectFullList(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap) {
        QueryWrapper<M> wrapper = buildWrapper(columnMap);
        return getBaseMapper().selectList(wrapper);
    }

    
    public List<M> selectByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap) {
        QueryWrapper<M> wrapper = buildWrapper(columnMap);
        return getBaseMapper().selectList(wrapper);
    }

    /**
     * 分页查询
     */
    public <P extends IPage<M>> P selectPage(P page, Wrapper<M> queryWrapper) {
        return getBaseMapper().selectPage(page, queryWrapper);
    }

    /**
     * 根据条件统计数量
     */
    public long selectCount(Wrapper<M> queryWrapper) {
        return getBaseMapper().selectCount(queryWrapper);
    }

    /**
     * 根据 Wrapper 查询单条
     */
    public M selectOne(Wrapper<M> queryWrapper) {
        return getBaseMapper().selectOne(queryWrapper);
    }

    /**
     * 根据 ID 更新
     */
    public boolean updateById(M data) {
        return getBaseMapper().updateById(data) > 0;
    }

    /**
     * 根据 Wrapper 删除
     */
    public int delete(Wrapper<M> queryWrapper) {
        return getBaseMapper().delete(queryWrapper);
    }

    /**
     * 构建查询条件 Wrapper
     */
    private QueryWrapper<M> buildWrapper(Map<String, Object> columnMap) {
        QueryWrapper<M> wrapper = new QueryWrapper<>();
        if (columnMap != null && !columnMap.isEmpty()) {
            columnMap.forEach((key, value) -> {
                if (value != null) {
                    wrapper.eq(key, value);
                }
            });
        }
        return wrapper;
    }

    /**
     * 通过反射设置字段值
     */
    private void setFieldValue(M data, String fieldName, Object value) {
        try {
            Field field = data.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(data, value);
        } catch (Exception ignored) {
            // 字段不存在则静默忽略
        }
    }
}

