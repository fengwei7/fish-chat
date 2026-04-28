# Fish-Chat 即时聊天后端

Fish-Chat 是一个基于 Spring Boot 的即时聊天后端系统，支持用户认证、实时 WebSocket 通讯、消息存储等功能。

## 技术栈

- **Spring Boot**: 2.7.18
- **Java**: 1.8
- **MySQL**: 用户数据存储
- **MongoDB**: 聊天记录存储
- **Redis**: 在线状态缓存
- **MyBatis-Plus**: ORM 框架
- **Sa-Token**: 权限认证
- **WebSocket**: 实时通讯

## 项目结构

```
fish-chat/
├── fish-chat-common/          # 公共模块
│   ├── result/                # 统一返回结果
│   ├── exception/             # 全局异常处理
│   └── constants/             # 常量定义
│
├── fish-chat-core/            # 核心业务模块
│   ├── entity/                # 实体类
│   ├── dto/                   # 数据传输对象
│   ├── mapper/                # MyBatis Mapper
│   ├── repository/            # 仓储层
│   ├── service/               # 业务服务
│   └── handler/               # 消息处理器
│
└── fish-chat-bootstrap/       # 启动模块
    ├── controller/            # REST Controller
    ├── config/                # 配置类
    └── resources/             # 配置文件
```

## 快速开始

### 方式一：Docker Compose（推荐）

1. 确保已安装 Docker 和 Docker Compose

2. 启动所有服务
```bash
docker-compose up -d
```

3. 查看日志
```bash
docker-compose logs -f app
```

4. 访问应用
- API 地址：http://localhost:8080
- 测试页面：http://localhost:8080/chat-test.html

### 方式二：本地运行

#### 前置条件
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.4+
- Redis 6.0+

#### 步骤

1. 克隆项目
```bash
git clone <repository-url>
cd fish-chat
```

2. 配置环境变量（可选）
```bash
cp .env .env
# 编辑 .env 文件填入实际配置
```

3. 构建项目
```bash
mvn clean package -DskipTests
```

4. 运行应用
```bash
java -jar fish-chat-bootstrap/target/fish-chat-bootstrap-0.0.1-SNAPSHOT.jar
```

## API 接口

### 认证接口

#### 用户注册
```bash
POST /auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "nickname": "Test User"
}
```

#### 用户登录
```bash
POST /auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "xxx-xxx-xxx",
    "userId": 1,
    "username": "testuser"
  }
}
```

#### 用户登出
```bash
POST /auth/logout
Authorization: Bearer {token}
```

### WebSocket 连接

连接地址：
```
ws://localhost:8080/websocket/chat?token={satoken}
```

消息格式：
```json
{
  "type": "chat",
  "to": "目标用户 ID",
  "content": "消息内容"
}
```

心跳消息：
```json
{
  "type": "ping"
}
```

## 功能验证

### 使用测试页面

1. 打开浏览器访问：http://localhost:8080/chat-test.html

2. 注册两个账号（或使用已有账号登录）

3. 在两个不同的浏览器窗口（或无痕模式）分别登录两个账号

4. 输入对方用户 ID，发送消息进行测试

### 使用 curl 测试

```bash
# 注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123","nickname":"User 1"}'

# 登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}'

# 获取用户信息
curl -X GET http://localhost:8080/userPO/info \
  -H "satoken: {your-token}"
```

## 开发指南

### 运行测试
```bash
mvn test
```

### 查看测试覆盖率
```bash
mvn clean test jacoco:report
```

### 构建 Docker 镜像
```bash
docker-compose build
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_URL | MySQL 连接 URL | jdbc:mysql://localhost:3306/fish_chat |
| DB_USERNAME | MySQL 用户名 | root |
| DB_PASSWORD | MySQL 密码 | (空) |
| MONGODB_URI | MongoDB 连接 URI | mongodb://localhost:27017/fishchat |
| REDIS_HOST | Redis 主机 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |
| REDIS_PASSWORD | Redis 密码 | (空) |

## 重构改进点

本次重构主要改进了以下方面：

### 架构优化
✅ 三模块 Maven 结构（common、core、bootstrap），职责清晰
✅ 消除静态依赖注入反模式
✅ 统一异常处理和返回结果

### 安全加固
✅ 移除硬编码密码，使用环境变量
✅ CORS 配置修复
✅ WebSocket Token 验证增强

### 性能优化
✅ Redis 使用 SCAN 替代 KEYS 命令
✅ Spring Cache 缓存用户数据

### 代码质量
✅ 策略模式处理 WebSocket 消息
✅ 工厂模式构建消息
✅ 参数校验（JSR-303）

### 测试完善
✅ 单元测试示例
✅ 集成测试框架（Testcontainers）
✅ 完整的测试页面

## 常见问题

### Q: 数据库连接失败？
A: 检查 MySQL 是否启动，确认环境变量配置正确。

### Q: WebSocket 连接失败？
A: 确保 Token 有效，检查网络防火墙设置。

### Q: MongoDB 保存消息失败？
A: 确认 MongoDB 服务正常运行，检查连接 URI 配置。

## License

MIT License

## 联系方式

如有问题请提交 Issue 或联系开发团队。
