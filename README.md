

# Fish Chat 鱼聊

Fish Chat（鱼聊）是一个基于 Java + Netty WebSocket 实现的实时聊天系统，采用微服务架构设计，支持用户注册登录、实时消息收发、在线状态管理等功能。

## 技术栈

- **后端框架**：Spring Boot 2.x
- **数据库**：MySQL + MongoDB
- **缓存**：Redis
- **认证**：Sa-Token
- **WebSocket**：Netty
- **ORM**：MyBatis Plus

## 项目结构

```
fish-chat/
├── bootstrap/          # 启动模块
│   └── src/main/java/com/fish/chat/bootstrap/
│       └── config/     # 配置类
├── common/             # 通用模块
│   └── src/main/java/com/fish/chat/common/
│       ├── constants/ # 常量定义
│       ├── entity/    # 实体基类
│       ├── enums/     # 枚举
│       ├── exception/# 异常处理
│       ├── properties/# 配置属性
│       ├── redisutils/# Redis工具
│       ├── repository/# 基础仓储
│       ├── result/    # 统一响应
│       └── utils/     # 通用工具
└── core/              # 核心业务模块
    └── src/main/java/com/fish/chat/core/
        ├── controller/# 控制器
        ├── entity/   # 业务实体
        ├── mapper/   # MyBatis Mapper
        ├── netty/   # Netty WebSocket
        ├── repository/# 业务仓储
        └── service/  # 业务服务
```

## 主要功能

### 用户模块
- 用户注册
- 用户登录
- 获取/更新用户资料
- 用户搜索

### 消息模块
- 实时聊天消息收发
- 消息类型支持（文本等）
- 消息历史存储

### 在线状态
- 用户在线状态管理
- 在线用户列表
- 心跳检测

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.x
- MySQL 5.7+
- MongoDB 4.x
- Redis 5.x

### 编译运行

```bash
# 编译项目
mvn clean package

# 启动应用
java -jar fish-chat-bootstrap/target/fish-chat-bootstrap.jar
```

### Docker 部署

```bash
docker-compose up -d
```

## 配置说明

配置文件位于 `bootstrap/src/main/resources/`

- `application.yaml` - 主配置
- `application-dev.yaml` - 开发环境配置
- `application-prod.yaml` - 生产环境配置

### 核心配置项

```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      host: localhost
      port: 27017
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true

netty:
  websocket:
    port: 8081
```

## API 接口

### 认证接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/auth/register` | POST | 用户注册 |
| `/auth/login` | POST | 用户登录 |
| `/auth/logout` | POST | 用户登出 |

### 用户接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/user/profile` | GET | 获取当前用户资料 |
| `/user/profile` | POST | 更新用户资料 |
| `/user/{code}` | GET | 根据编码获取用户 |

### 文件接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/file/upload` | POST | 文件上传 |
| `/file/download/{fileName}` | GET | 文件下载 |

### WebSocket

WebSocket 连接地址：`ws://localhost:8081/ws`

连接参数：
- `token`: 认证令牌

## 许可证

MIT License