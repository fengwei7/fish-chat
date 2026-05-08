package com.fish.chat.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用分页结果封装
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> data;

    /**
     * 当前页码（从0开始）
     */
    private long pageNum;

    /**
     * 每页数量
     */
    private long pageSize;

    /**
     * 总记录数
     */
    private long total;

    public PageResult() {
    }

    public PageResult(List<T> data, long pageNum, long pageSize, long total) {
        this.data = data;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }

    /**
     * 快速构建分页结果
     */
    public static <T> PageResult<T> of(List<T> data, long pageNum, long pageSize, long total) {
        return new PageResult<>(data, pageNum, pageSize, total);
    }

    /**
     * 对已有列表做内存分页
     */
    public static <T> PageResult<T> ofPage(List<T> data, int pageNum, int pageSize) {
        int total = data.size();
        int fromIndex = pageNum * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<T> subList = fromIndex < total ? data.subList(fromIndex, toIndex) : new ArrayList<>();
        return new PageResult<>(subList, pageNum, pageSize, total);
    }
}
