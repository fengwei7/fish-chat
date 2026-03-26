-- =====================================================
-- Fish Chat 数据库初始化脚本
-- 数据库：MySQL 8.0
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `fish_chat` 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

USE `fish_chat`;

-- =====================================================
-- 用户表
-- =====================================================
DROP TABLE IF EXISTS `t_user`;

CREATE TABLE `t_user` (
  `id` BIGINT(20) NOT NULL COMMENT '主键 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（SHA256+ 盐值加密）',
  `salt` VARCHAR(50) NOT NULL COMMENT '密码盐值',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `profile` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT(4) DEFAULT '1' COMMENT '状态：0-禁用 1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(4) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一索引',
  UNIQUE KEY `uk_email` (`email`) COMMENT '邮箱唯一索引',
  UNIQUE KEY `uk_mobile` (`mobile`) COMMENT '手机号唯一索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_deleted` (`deleted`) COMMENT '删除标记索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 插入测试数据（可选）
-- =====================================================
-- 以下测试数据的密码均为 "123456"，使用 SHA256+ 随机盐值加密
-- 实际使用时请通过注册接口创建用户

-- 示例：手动计算加密密码（仅供参考，不建议直接使用）
-- salt: test_salt_123456
-- password: SHA256("123456" + "test_salt_123456")
-- INSERT INTO `t_user` (`id`, `username`, `password`, `salt`, `nickname`, `status`) 
-- VALUES (1, 'testuser', '加密后的密码哈希值', 'test_salt_123456', '测试用户', 1);

-- =====================================================
-- 说明
-- =====================================================
-- 1. 聊天记录存储在 MongoDB 中，不在 MySQL 中创建表
-- 2. 用户在线状态存储在 Redis 中，不在 MySQL 中创建表
-- 3. 所有密码均使用 SHA256 + 随机盐值加密存储
-- 4. 使用 MyBatis-Plus 的自动填充功能管理 create_time 和 update_time
-- 5. 使用逻辑删除（deleted 字段），物理删除数据时请谨慎操作
