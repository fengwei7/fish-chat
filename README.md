# Fish-Chat 🐟

基于 **Netty + Spring Boot + Vue 3** 的即时通信服务，支持单聊、群聊、频道广播、好友管理、文件传输等完整 IM 功能。

## 功能特性

- **用户系统**：注册、登录、登出、个人资料管理、用户搜索
- **好友系统**：发送好友申请、接受/拒绝、删除好友、查看在线状态
- **群组系统**：创建群组、解散群组、添加成员、群成员管理
- **频道系统**：创建频道、订阅/取消订阅、频道广播
- **实时通信**：基于 Netty 的 WebSocket 长连接，支持断线重连与心跳保活
- **消息类型**：文本、图片、文件，消息历史分页查询与断线同步
- **文件传输**：支持图片/文件上传与下载
- **多端在线**：基于 Sa-Token 的统一认证，Redis 会话共享
- **分布式锁**：基于 Redisson 实现并发安全控制

## 技术栈

### 后端

| 组件 | 版本 |
|------|------|
| JDK | 1.8 |
| Spring Boot | 2.7.18 |
| Netty | 4.1.100.Final |
| MyBatis-Plus | 3.5.5 |
| Sa-Token | 1.45.0 |
| MySQL | 8.0 |
| MongoDB | 7.x |
| Redis | 7.x |
| Druid | 1.2.20 |
| Hutool | 5.8.26 |
| FastJSON2 | 2.0.43 |
| Lombok | 1.18.30 |
| Redisson | 3.25.0 |
| MapStruct | 1.5.5.Final |

### 前端

| 组件 | 版本 |
|------|------|
| Vue | 3.5.18 |
| Element Plus | 2.13.1 |
| Vite | 7.1.11 |
| Vue Router | 4.5.1 |
| Pinia | 3.0.3 |
| Axios | 1.15.2 |
| Pinia PersistedState | 4.4.1 |

## 项目架构

```
fish-chat
├── bootstrap    启动模块 — 应用入口 + 全局配置（CORS、Sa-Token、静态资源）
├── common       公共模块 — 统一返回结果、异常处理、工具类、Redis 封装
├── core         核心模块 — 业务逻辑、数据访问、Netty WebSocket 引擎
│   ├── chat/    聊天引擎 — 消息协议、会话管理、房间模型
│   ├── controller/  REST API 控制器
│   ├── service/     业务服务层
│   ├── repository/  数据仓库层（MyBatis-Plus + MongoDB）
│   └── netty/       Netty WebSocket 服务器与处理器
└── webUI        前端模块 — Vue 3 单页应用
    ├── src/api/      API 接口封装
    ├── src/views/    页面视图
    ├── src/stores/   Pinia 状态管理
    └── src/router/   路由配置
```

### 指令类型

| cmd | 方向 | 说明 |
|-----|------|------|
| `MSG` | C↔S | 聊天消息 |
| `NOTIFY` | S→C | 系统通知 |
| `ERROR` | S→C | 错误提示 |
| `ACK` | S→C | 消息确认 |
| `HEARTBEAT` | C→S | 心跳保活（30s 间隔） |

### 房间编码规则

| 类型 | 编码格式 | 示例 |
|------|----------|------|
| 单聊 | `private:<小code>:<大code>` | `private:userA:userB` |
| 群聊 | `group:<groupCode>` | `group:xxx` |
| 频道 | `channel:<channelCode>` | `channel:xxx` |

## 快速开始

### 环境要求

- JDK 8+
- Node.js 20+
- Maven 3.9+
- MySQL 8.0
- MongoDB 7
- Redis 7

### 1. 初始化数据库

```bash
mysql -u root -p < bootstrap/src/main/resources/schema.sql
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入数据库连接信息
```

### 3. 启动后端

```bash
mvn clean package -DskipTests
java -jar bootstrap/target/bootstrap.jar
```

后端服务默认端口：
- HTTP API：`8080`
- WebSocket：`8081`

### 4. 启动前端

```bash
cd plugin-web-client/webUI
pnpm install
pnpm dev
```

前端默认地址：`http://localhost:5173`

### 5. Docker 一键部署

```bash
docker-compose up -d
```
## 核心架构特点

- **分层架构**：bootstrap(启动) -> core(核心业务) -> common(公共组件)
- **数据访问**：Repository 模式封装 MyBatis-Plus 和 MongoDB 操作
- **会话管理**：基于 Netty 的 WebSocket 长连接，支持心跳保活和断线重连
- **认证授权**：Sa-Token 统一认证，支持多端登录和 Redis 会话共享
- **分布式锁**：Redisson 实现并发安全控制
- **枚举管理**：业务状态统一使用枚举，避免魔法字符串
- **统一响应**：所有 API 返回统一的 Result 格式
- **异常处理**：全局异常处理器，统一错误码和错误信息

## 环境变量配置

项目支持通过 `.env` 文件或环境变量配置：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=fish_chat
DB_USERNAME=root
DB_PASSWORD=your_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MongoDB 配置
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_DB=fish_chat
MONGO_USERNAME=
MONGO_PASSWORD=

# 应用配置
SPRING_PROFILES_ACTIVE=dev
```

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化 POJO 代码
- 使用 MapStruct 进行对象映射
- 统一使用枚举管理业务状态
- Repository 层封装所有数据访问操作

### 模块职责

- **bootstrap**: 应用启动入口、全局配置(CORS、Sa-Token、静态资源)
- **core**: 业务逻辑实现、Netty WebSocket 服务器、REST API 控制器
- **common**: 通用工具类、统一返回结果、异常处理、Redis 封装
- **webUI**: Vue 3 前端应用