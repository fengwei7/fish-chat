/*
 * 群组相关表结构
 */

-- 创建群组表
CREATE TABLE `t_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '群组名称',
  `description` varchar(500) DEFAULT NULL COMMENT '群组描述',
  `owner_id` bigint(20) NOT NULL COMMENT '群主ID',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '群组头像',
  `notice` varchar(1000) DEFAULT NULL COMMENT '群组公告',
  `max_members` int(11) DEFAULT '1000' COMMENT '群组最大成员数',
  `type` tinyint(4) DEFAULT '1' COMMENT '群组类型：1-普通群 2-公开群',
  `join_permission` tinyint(4) DEFAULT '1' COMMENT '入群验证方式：0-无需验证 1-需要验证 2-不允许加群',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '群组状态：0-解散 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

-- 创建群组成员表
CREATE TABLE `t_group_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `nickname` varchar(100) DEFAULT NULL COMMENT '用户在群组中的昵称',
  `role` tinyint(4) NOT NULL DEFAULT '1' COMMENT '用户角色：1-普通成员 2-管理员 3-群主',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-已退出 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';