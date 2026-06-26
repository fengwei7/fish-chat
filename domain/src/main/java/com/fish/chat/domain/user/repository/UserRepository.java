package com.fish.chat.domain.user.repository;

import com.fish.chat.domain.base.BaseRepository;
import com.fish.chat.domain.user.model.entity.User;

import java.util.List;

/**
 * 用户仓储接口
 * 
 * 定义用户领域的数据访问操作规范
 * 继承 BaseRepository 获得基础 CRUD 能力
 * 
 * 设计原则：
 * - 依赖倒置：domain层定义接口，infrastructure层实现
 * - 职责单一：只定义用户相关的数据访问操作
 * - 业务语义：方法名体现业务含义（如 findByUsername）
 */
public interface UserRepository extends BaseRepository<User> {
    
    // ==================== 用户特定查询 ====================
    
    /**
     * 根据用户名查询
     * 
     * @param username 用户名
     * @return 用户实体，不存在时返回 null
     */
    User findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据关键字搜索用户（用户名或昵称模糊匹配）
     * 
     * @param keyword 关键字
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<User> searchByKeyword(String keyword, int pageNum, int pageSize);
    
    /**
     * 统计关键字匹配的用户数
     * 
     * @param keyword 关键字
     * @return 用户数
     */
    long countByKeyword(String keyword);
}
