package com.fish.chat.infrastructure.persistence.converter;

import com.fish.chat.common.entity.BasePO;
import com.fish.chat.domain.base.BaseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基础转换器接口
 * 
 * 定义 PO（Persistent Object）和 Domain Entity 之间的转换规范
 * 
 * 设计原则：
 * - 职责单一：只负责数据转换，不包含业务逻辑
 * - 类型安全：通过泛型保证编译期类型检查
 * - 双向转换：支持 PO ↔ Entity 双向转换
 * 
 * 实现方式：
 * - 使用 MapStruct 实现高性能转换（推荐）
 * - 或手动编写转换方法（简单场景）
 * 
 * @param <E> Domain Entity 类型，必须继承 BaseEntity
 * @param <P> PO 类型（Persistent Object），必须继承 BasePO
 */
public interface BaseConverter<E extends BaseEntity, P extends BasePO> {

    /**
     * 模型转DO
     *
     * @param e 模型实体
     * @return 数据实体
     */
     P toPO(E e);

    /**
     * DO转模型
     *
     * @param p 数据实体
     * @return 模型实体
     */
    E toEntity(P p);

    /**
     * 批量转换：PO列表 → Entity列表
     *
     * @param list PO集合
     * @return Entity集合
     */
    List<E> toEntity(List<P> list);

    /**
     * 批量转换：Entity列表 → PO列表
     *
     * @param list Entity集合
     * @return PO集合
     */
    List<P> toPO(List<E> list);

    /**
     * 批量转换：Entity列表 → PO列表（带外键设置）
     * 
     * @param list Entity集合
     * @param foreignMap 外键字段映射（key: PO字段名, value: 值）
     * @return PO集合
     */
    default List<P> toPO(List<E> list, Map<String, Object> foreignMap) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(e -> toPO(e, foreignMap))
                .collect(Collectors.toList());
    }

    /**
     * 单个转换：Entity → PO（带外键设置）
     * 
     * @param entity Entity实例
     * @param foreignMap 外键字段映射（key: PO字段名, value: 值）
     * @return PO实例
     */
    default P toPO(E entity, Map<String, Object> foreignMap) {
        P po = toPO(entity);
        if (foreignMap != null && !foreignMap.isEmpty()) {
            // 通过反射设置外键字段到 PO
            foreignMap.forEach((key, value) -> {
                try {
                    java.lang.reflect.Field field = po.getClass().getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(po, value);
                } catch (Exception ignored) {
                    // 字段不存在则忽略
                }
            });
        }
        return po;
    }
}
