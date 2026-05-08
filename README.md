# Fish-Chat 🐟

基于 **Netty + Spring Boot** 的即时通信服务，支持单聊、群聊、频道。

## 技术栈

| 组件 | 版本 |
|------|------|
| JDK | 1.8 |
| Spring Boot | 2.7.18 |
| Netty | 4.1.100 |
| MyBatis-Plus | 3.5.5 |
| MongoDB | 7 |
| Redis | 7 |
| MySQL | 8.0 |
| Sa-Token | 1.44.0 |

## 架构

```
fish-chat
├── bootstrap    启动模块 — 应用入口 + 全局配置
├── common       公共模块 — 工具类、异常处理、统一返回
└── core         核心模块 — 业务逻辑、数据访问、Netty
    └── chat/    聊天引擎 — 消息协议、会话管理、房间模型
```

## 快速开始

### 1. 环境准备

- JDK 8+
- MySQL 8.0
- MongoDB 7
- Redis 7
- Maven 3.9+

### 2. 初始化数据库

```sql
source bootstrap/src/main/resources/schema.sql
```

### 3. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入真实的数据库连接信息
```

### 4. 编译运行

```bash
mvn clean package -DskipTests
java -jar bootstrap/target/bootstrap.jar
```

### 5. Docker 运行

```bash
docker-compose up -d
```

## WebSocket 连接

```
ws://localhost:8081/ws?token=your_sa_token
```

### 消息协议

```json
// 发送消息
{"cmd":"MSG","reqId":"uuid","body":{"roomCode":"room_xxx","roomType":"PRIVATE","msgType":"TEXT","content":"hello"}}

// 接收消息
{"cmd":"MSG","code":0,"reqId":"uuid","body":{"msgId":"mongo_id","senderId":"user_code","senderName":"name","roomCode":"room_xxx","content":"hello","timestamp":17000000}}
```

## REST API

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/register | 注册 |
| POST | /auth/login | 登录 |
| POST | /auth/logout | 登出 |

### 用户
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /user/profile | 个人信息 |
| POST | /user/profile | 修改信息 |
| GET | /user/{code} | 按code查用户 |
| GET | /user/search?keyword= | 搜索用户 |
| GET | /user/online | 在线用户 |

### 好友
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /friends | 添加好友 |
| POST | /friends/accept | 接受请求 |
| DELETE | /friends/{code} | 删除好友 |
| GET | /friends | 好友列表 |

### 群组
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /groups | 创建群组 |
| GET | /groups/{code} | 群组信息 |
| DELETE | /groups/{code} | 解散群组 |
| POST | /groups/{code}/members | 添加成员 |
| DELETE | /groups/{code}/members/{code} | 移除成员 |
| GET | /groups/my | 我的群组 |
| GET | /groups/search?keyword= | 搜索群组 |

### 频道
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /channels | 创建频道 |
| GET | /channels/{code} | 频道信息 |
| POST | /channels/{code}/subscribe | 订阅 |
| DELETE | /channels/{code}/subscribe | 取消订阅 |
| GET | /channels/my | 我的频道 |

### 消息
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /messages/{roomCode} | 历史消息（分页） |
| GET | /messages/{roomCode}/sync?after= | 同步消息 |

### 文件
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /file/upload | 上传 |
| GET | /file/download/{name} | 下载 |
