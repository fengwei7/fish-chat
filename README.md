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

### 模块职责

- **bootstrap**: 应用启动入口、全局配置(CORS、Sa-Token、静态资源)
- **core**: 业务逻辑实现、Netty WebSocket 服务器、REST API 控制器
- **common**: 通用工具类、统一返回结果、异常处理、Redis 封装
- **webUI**: Vue 3 前端应用