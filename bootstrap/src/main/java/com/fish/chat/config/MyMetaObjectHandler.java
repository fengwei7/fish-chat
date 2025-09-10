package com.fish.chat.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

//   在每次insert和update时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        // 如果create_time为null，则自动填充
        Object createTime = getFieldValByName("createTime", metaObject);
        if (createTime == null) {
            this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        }
        
        // 如果update_time为null，则自动填充
        Object updateTime = getFieldValByName("updateTime", metaObject);
        if (updateTime == null) {
            this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时总是更新update_time字段
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}