-- ============================================
-- Fish-Chat 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS fish_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fish_chat;

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(64)  NOT NULL COMMENT '用户唯一标识',
    username    VARCHAR(64)  NOT NULL COMMENT '用户名',
    password    VARCHAR(256) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    avatar_url  VARCHAR(256) DEFAULT NULL COMMENT '头像URL',
    profile     VARCHAR(512) DEFAULT NULL COMMENT '个人简介',
    email       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    mobile      VARCHAR(32)  DEFAULT NULL COMMENT '手机号',
    status      INT          DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    updater     VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    UNIQUE KEY uk_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 群组表
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_group (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(64)  NOT NULL COMMENT '群唯一标识',
    name        VARCHAR(128) NOT NULL COMMENT '群名称',
    avatar      VARCHAR(256) DEFAULT NULL COMMENT '群头像',
    owner_code  VARCHAR(64)  NOT NULL COMMENT '群主用户code',
    notice      VARCHAR(1024) DEFAULT NULL COMMENT '群公告',
    max_members INT          DEFAULT 200 COMMENT '最大成员数',
    status      INT          DEFAULT 1 COMMENT '状态：0-解散 1-正常',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    updater     VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    INDEX idx_owner (owner_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组表';

-- ----------------------------
-- 群成员表
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_group_member (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    group_code  VARCHAR(64)  NOT NULL COMMENT '群code',
    user_code   VARCHAR(64)  NOT NULL COMMENT '用户标识',
    role        INT          DEFAULT 0 COMMENT '角色：0-成员 1-管理员 2-群主',
    join_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_group_user (group_code, user_code),
    INDEX idx_user_code (user_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员表';

-- ----------------------------
-- 频道表
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_channel (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(64)  NOT NULL COMMENT '频道唯一标识',
    name        VARCHAR(128) NOT NULL COMMENT '频道名称',
    avatar      VARCHAR(256) DEFAULT NULL COMMENT '频道头像',
    owner_code  VARCHAR(64)  NOT NULL COMMENT '创建者用户code',
    description VARCHAR(512) DEFAULT NULL COMMENT '频道描述',
    status      INT          DEFAULT 1 COMMENT '状态：0-关闭 1-正常',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(64)  DEFAULT NULL COMMENT '创建人',
    updater     VARCHAR(64)  DEFAULT NULL COMMENT '更新人',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    INDEX idx_owner (owner_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道表';

-- ----------------------------
-- 频道订阅表
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_channel_member (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    channel_code VARCHAR(64)  NOT NULL COMMENT '频道code',
    user_code   VARCHAR(64)  NOT NULL COMMENT '用户标识',
    role        INT          DEFAULT 0 COMMENT '角色：0-订阅者 1-管理员 2-创建者',
    join_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_user (channel_code, user_code),
    INDEX idx_user_code (user_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道订阅表';

-- ----------------------------
-- 好友关系表
-- ----------------------------
CREATE TABLE IF NOT EXISTS t_friend (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_code   VARCHAR(64)  NOT NULL COMMENT '用户标识',
    friend_code VARCHAR(64)  NOT NULL COMMENT '好友用户标识',
    remark      VARCHAR(64)  DEFAULT NULL COMMENT '备注名',
    status      INT          DEFAULT 1 COMMENT '状态：0-待确认 1-已确认 2-已拒绝',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_friend (user_code, friend_code),
    INDEX idx_friend_code (friend_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';
