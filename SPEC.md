# Fish-Chat 即时通信服务 — 技术规格说明书

> 项目基于 Spring Boot 2.7 + Netty 4.1 + Sa-Token 的即时通信后端服务

---

## 一、项目概览

| 项目 | 信息 |
|------|------|
| 名称 | fish-chat |
| 描述 | 基于 Netty 的即时通信服务（半成品） |
| 技术栈 | Spring Boot 2.7.18 / Netty 4.1.100 / MyBatis-Plus 3.5.5 / MongoDB / Redis / Sa-Token 1.44.0 / MySQL 8.0 |
| 构建 | Maven 多模块 |
| JVM 目标 | 1.8（当前用 Java 17 编译） |
| 端口 | HTTP 8080 / WebSocket 8081 |

---

## 二、模块架构

```
fish-chat
├── bootstrap   启动模块 — 应用入口 + 全局配置
├── common      公共模块 — 工具类、异常处理、统一返回、Redis工具
└── core        核心模块 — 业务逻辑、数据访问、Netty WebSocket
```

### 2.1 common（公共模块）
| 类 | 说明 |
|----|------|
| `Result<T>` | 统一API响应：code / message / data / timestamp |
| `BasePO` | 实体基类：id / code / status / createTime / updateTime / creator / updater / deleted |
| `BaseRepository<M>` | 泛型 CRUD 抽象层，封装 MyBatis-Plus 基本操作 |
| `BusinessException` | 业务异常（带错误码） |
| `GlobalExceptionHandler` | 全局异常处理：BusinessException、参数校验、未知异常 |
| `ErrorCodeEnum` | 错误码枚举（通用1xxx / 业务2xxx / 文件3xxx） |
| `RedisUtil` | Redis操作 + 分布式锁（Lua脚本安全释放） |
| `AuthConstants` | 认证常量：Token Header、在线用户Key前缀等 |
| `FileUploadProperties` | 文件上传配置属性 |
| `FileUploadUtil` | 文件上传工具类 |

### 2.2 core（核心模块）
| 分层 | 说明 |
|------|------|
| **Controller** | AuthController（注册/登录/登出）、UserProfileController（个人信息）、FileUploadController（上传/下载） |
| **Service** | AuthServiceImpl（注册+密码SHA256+salt加密/登录+Sa-Token/登出）、UserServiceImpl（个人信息增改查） |
| **Repository** | UserRepository（MySQL MyBatis-Plus）、ChatMessageRepository（MongoDB Spring Data）、UserOnlineRepository（Redis） |
| **Mapper** | UserMapper（MyBatis-Plus BaseMapper） |
| **Entity/PO** | UserPO（MySQL t_user表）、ChatMessage（MongoDB chat_messages集合） |
| **DTO/Req** | AuthDTO、UserDTO、LoginRequest、RegisterRequest、UserUpdateRequest |
| **Netty** | 见下文 |

### 2.3 bootstrap（启动模块）
| 配置类 | 说明 |
|--------|------|
| CorsConfig | 全通配跨域配置 |
| MybatisPlusConfig | 分页插件 + 自动填充（code/uuid、时间、操作人、逻辑删除） |
| SaTokenConfigure | 登录拦截器（排除 /auth/login、/auth/register、静态资源）+ ServletFilter |
| StaticResourceConfig | 静态资源 + 上传文件目录映射 |

---

## 三、数据存储设计

### 3.1 MySQL — t_user（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK, AUTO) | 主键 |
| code | VARCHAR | 用户唯一标识（UUID，自动填充） |
| username | VARCHAR | 用户名（唯一） |
| password | VARCHAR | 密码（SHA256(username + salt)） |
| salt | VARCHAR(16) | 密码盐值 |
| nickname | VARCHAR | 昵称 |
| avatar_url | VARCHAR | 头像URL |
| profile | VARCHAR(255) | 个人简介 |
| email | VARCHAR | 邮箱 |
| mobile | VARCHAR(20) | 手机号 |
| status | INT | 状态：0-禁用 1-正常 |
| create_time | DATETIME | 创建时间（自动填充） |
| update_time | DATETIME | 更新时间（自动填充） |
| creator | VARCHAR | 创建人（自动填充） |
| updater | VARCHAR | 更新人（自动填充） |
| deleted | INT | 逻辑删除（自动填充 0） |

### 3.2 MongoDB — chat_messages（聊天消息）
| 字段 | 类型 | 说明 |
|------|------|------|
| _id | Long | 主键 |
| type | String | 消息类型：chat / system / image / file |
| from | String (indexed) | 发送方用户ID |
| to | String (indexed) | 接收方用户ID |
| content | String | 消息内容 |
| timestamp | Long (indexed) | 时间戳 |

### 3.3 Redis — 在线用户
- Key: `user:online:{userId}` → Value: UserDTO（JSON，过期时间1分钟）
- 心跳续期逻辑：每次 ping 更新 TTL

---

## 四、Netty WebSocket 设计

### 4.1 连接流程
```
Client                              Server
  |--- WS握手 (ws://host:8081/websocket/chat?token=xxx) -->|
  |                                                         | 提取 token
  |                                                         | StpUtil.getLoginIdByToken()
  |                                                         | 存入 Channel Attribute
  |                                                         | 保存到 Redis 在线列表
  |<-- {"type":"connect","content":"连接成功"} ---|
```

### 4.2 消息协议
| 方向 | 类型 | 说明 |
|------|------|------|
| 上行 | `{"type":"chat","to":"userId","content":"..."}` | 聊天消息 |
| 上行 | `{"type":"ping"}` | 心跳 |
| 下行 | `{"type":"chat","content":"..."}` | 聊天消息（转发） |
| 下行 | `{"type":"pong","content":""}` | 心跳响应 |
| 下行 | `{"type":"error","content":"..."}` | 错误消息 |
| 下行 | `{"type":"connect","content":"连接成功"}` | 连接确认 |

### 4.3 在线会话管理
- `ConcurrentHashMap<String, ChannelHandlerContext> onlineSessions` — 在线会话Map
- 连接：握手成功 → 放入 sessions + Redis
- 断连：channelInactive → 移除 sessions + Redis
- 消息路由：查 onlineSessions 找目标 → 直接转发

---

## 五、API 接口（REST）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /auth/register | 用户注册 | 免登录 |
| POST | /auth/login | 用户登录 | 免登录 |
| POST | /auth/logout | 用户登出 | 需要 |
| GET | /user/profile | 获取个人信息 | 需要 |
| POST | /user/profile | 更新个人信息 | 需要 |
| GET | /user/{code} | 按code查用户 | 需要 |
| POST | /file/upload | 上传文件 | 需要 |
| GET | /file/download/{name} | 下载文件 | 需要 |

---

## 六、已完成的功能 ✅

1. **用户注册/登录/登出** — SHA256+salt 密码加密，Sa-Token 管理会话
2. **个人信息管理** — 查看和更新昵称、头像、简介、邮箱、手机
3. **文件上传/下载** — 文件类型/大小校验，UUID重命名，静态资源映射
4. **WebSocket 实时通信** — Netty 服务器，Token 认证握手
5. **点对点聊天** — 在线用户间消息实时转发
6. **聊天记录持久化** — MongoDB 存储消息
7. **心跳机制** — ping/pong + Redis 续期
8. **在线状态管理** — Redis + 内存双记录
9. **统一异常处理** — 全局异常处理器，统一返回格式
10. **Docker 部署** — docker-compose（MySQL + MongoDB + Redis + APP）

---

## 七、缺失/待完善的功能 ❌

### P0 — 核心即时通讯基础（必须先完成）

| # | 功能 | 说明 | 涉及文件 |
|---|------|------|----------|
| 1 | **MySQL 初始化脚本** | 无 t_user 建表 SQL，项目无法首次启动 | 新增 `bootstrap/src/main/resources/schema.sql` |
| 2 | **好友系统** | 无好友添加/删除/列表/搜索 | 新增 `friend` 相关 entity/service/controller/表 |
| 3 | **消息记录查询 REST API** | Repository 定义好了但没暴露 HTTP 接口 | 新增 `MessageController` |
| 4 | **会话列表（最近联系人）** | 无会话聚合查询 | 新增 `conversation` 逻辑 |
| 5 | **群组/群聊** | 无群组创建/管理/群聊消息 | 新增 `group` 相关模块 |

### P1 — 消息增强

| # | 功能 | 说明 | 涉及文件 |
|---|------|------|----------|
| 6 | **消息已读/未读状态** | 无 read receipt 机制 | `ChatMessage` 加字段 + 逻辑 |
| 7 | **文件消息支持** | 文件上传已有，但 WebSocket 不能发文件消息 | `NettyWebSocketHandler` |
| 8 | **消息类型扩展** | 图片/语音/视频消息类型 | `ChatMessage.type` 扩展 |
| 9 | **分页拉取历史消息** | 现有 `findByFromAndToOrderByTimestampDesc` 已加分页但无 API | `MessageController` |
| 10 | **消息搜索** | 按内容搜索消息 | MongoDB text index + API |

### P2 — 系统增强

| # | 功能 | 说明 | 涉及文件 |
|---|------|------|----------|
| 11 | **WebSocket 断线重连** | 无 idle 检测 / 自动重连 | Handler 加 IdleStateHandler |
| 12 | **离线消息推送** | 用户不在线时暂存，上线后推送 | MongoDB + Redis 队列 |
| 13 | **Sa-Token Redis 集成** | 当前 Sa-Token 没连 Redis，多实例不能共享会话 | 加 `sa-token-dao-redis` 依赖 |
| 14 | **密码修改/找回** | 无修改密码接口 | AuthService 扩展 |
| 15 | **用户搜索** | 按用户名/昵称搜索用户 | UserController 扩展 |
| 16 | **WebSocket 集群支持** | 当前 onlineSessions 是本机内存，多实例不互通 | 引入 Redis pub/sub 或 MQ |

### P3 — 代码质量/配置修复

| # | 问题 | 说明 | 修复方式 |
|---|------|------|----------|
| 17 | **java.version=1.8 与 Java 17 不匹配** | pom.xml 写 1.8 但环境用 17 | 升级到 17 |
| 18 | **env 配置不一致** | .env=YourSecurePassword123 vs docker-compose=SecurePassword123 vs dev yaml=12345678 | 统一为 .env 驱动 |
| 19 | **MongoDB 认证未配置** | docker-compose 设了用户密码但 dev yaml 没用 | dev yaml 加 auth |
| 20 | **MapStruct 已声明未使用** | pom.xml 引入了但无 mapper 接口 | 移除或实现 |
| 21 | **ChatMessage.id 类型风险** | Long 做 MongoDB _id 在分片/集群下可能冲突 | 改为 String/ObjectId |
| 22 | **文件上传防止路径穿越** | `download/{fileName}` 需要校验路径 | 加 PathTraversal 校验 |

---

## 八、开发计划

### Phase 1 — 基础完善
1. 创建 MySQL 初始化 SQL（t_user 建表）
2. 统一环境配置（.env / yaml 对齐）
3. 修复 java.version 到 17
4. 实现消息记录查询 API
5. 实现好友系统（CRUD + 搜索）

### Phase 2 — 核心即时通讯
6. 实现会话列表
7. 实现消息已读/未读
8. 实现群组功能
9. WebSocket 断线重连 + 离线消息

### Phase 3 — 生产化
10. Sa-Token Redis 集成
11. WebSocket 集群（Redis pub/sub）
12. 文件消息、图片消息支持
13. 密码修改、用户搜索
14. 代码质量修复

---

## 九、数据库设计（补充）

### t_friend（好友关系表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| user_id | BIGINT | 用户ID |
| friend_id | BIGINT | 好友ID |
| remark | VARCHAR | 备注名 |
| status | INT | 0-待确认 1-已确认 |
| create_time | DATETIME | 创建时间 |

### t_group（群组表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| code | VARCHAR | 群唯一标识 |
| name | VARCHAR | 群名称 |
| avatar | VARCHAR | 群头像 |
| owner_id | BIGINT | 群主ID |
| notice | VARCHAR | 群公告 |
| max_members | INT | 最大人数 |
| status | INT | 状态 |
| create_time | DATETIME | |

### t_group_member（群成员表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | PK |
| group_id | BIGINT | 群ID |
| user_id | BIGINT | 用户ID |
| role | INT | 0-普通 1-管理员 2-群主 |
| nickname | VARCHAR | 群昵称 |
| join_time | DATETIME | 入群时间 |

---

> ✅ **编译通过** — 当前代码可以成功 `mvn compile`
> 
> 请审阅此 Spec 文档，确认后我按照 Phase 1 → Phase 2 → Phase 3 顺序进行开发
