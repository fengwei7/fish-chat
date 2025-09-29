create table fish_chat.t_group
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    name            varchar(100)                       not null comment '群组名称',
    description     varchar(500)                       null comment '群组描述',
    owner_id        bigint                             not null comment '群主ID',
    avatar_url      varchar(500)                       null comment '群组头像',
    notice          varchar(1000)                      null comment '群组公告',
    max_members     int      default 1000              null comment '群组最大成员数',
    type            tinyint  default 1                 null comment '群组类型：1-普通群 2-公开群',
    join_permission tinyint  default 1                 null comment '入群验证方式：0-无需验证 1-需要验证 2-不允许加群',
    status          tinyint  default 1                 not null comment '群组状态：0-解散 1-正常',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         tinyint  default 0                 not null comment '逻辑删除标识：0-未删除 1-已删除'
)
    comment '群组表';

create index idx_owner_id
    on fish_chat.t_group (owner_id);

create index idx_status
    on fish_chat.t_group (status);

create table fish_chat.t_group_member
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    group_id    bigint                             not null comment '群组ID',
    user_id     bigint                             not null comment '用户ID',
    nickname    varchar(100)                       null comment '用户在群组中的昵称',
    role        tinyint  default 1                 not null comment '用户角色：1-普通成员 2-管理员 3-群主',
    status      tinyint  default 1                 not null comment '状态：0-已退出 1-正常',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 not null comment '逻辑删除标识：0-未删除 1-已删除',
    constraint uk_group_user
        unique (group_id, user_id)
)
    comment '群组成员表';

create index idx_group_id
    on fish_chat.t_group_member (group_id);

create index idx_status
    on fish_chat.t_group_member (status);

create index idx_user_id
    on fish_chat.t_group_member (user_id);

create table fish_chat.t_table
(
    id   int auto_increment
        primary key,
    name int null
);

create table fish_chat.t_user
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    username    varchar(50)                        not null comment '用户名',
    password    varchar(100)                       not null comment '密码',
    salt        varchar(50)                        null,
    nickname    varchar(50)                        null comment '昵称',
    avatar_url  varchar(255)                       null comment '头像',
    profile     varchar(255)                       null comment '简介',
    email       varchar(100)                       null comment '邮箱',
    mobile      varchar(20)                        null comment '手机号',
    status      int      default 1                 not null comment '状态：0-禁用 1-正常',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     int      default 0                 not null comment '逻辑删除标识：0-未删除 1-已删除',
    constraint uk_username
        unique (username)
)
    comment '用户表';

create index idx_email
    on fish_chat.t_user (email);

create index idx_mobile
    on fish_chat.t_user (mobile);

