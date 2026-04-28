# Fish-Chat 部署指南

## 前置要求

### 必需软件
- **JDK**: 1.8+ (推荐 JDK 8 或 JDK 11)
- **Maven**: 3.6+
- **Docker**: 20.10+ (可选，用于 Docker Compose 部署)
- **Docker Compose**: 1.29+ (可选)

### 数据库和服务
如果使用本地运行（非 Docker），需要安装：
- **MySQL**: 8.0+
- **MongoDB**: 4.4+
- **Redis**: 6.0+

## 部署方式

### 方式一：Docker Compose（最简单）

适合快速体验和测试，一键启动所有服务。

```bash
# 1. 进入项目目录
cd fish-chat

# 2. 启动所有服务（包括 MySQL、MongoDB、Redis 和应用）
docker-compose up -d

# 3. 查看日志
docker-compose logs -f app

# 4. 停止服务
docker-compose down

# 5. 停止并删除数据卷（彻底清理）
docker-compose down -v
```

**访问地址：**
- 应用 API: http://localhost:8080
- 测试页面：http://localhost:8080/chat-test.html
- MySQL: localhost:3306
- MongoDB: localhost:27017
- Redis: localhost:6379

### 方式二：本地运行（适合开发）

#### 步骤 1：安装依赖

确保已安装以下软件：
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- MongoDB 4.4+
- Redis 6.0+

#### 步骤 2：配置数据库

**MySQL:**
```sql
CREATE DATABASE fish_chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**MongoDB:**
```bash
mongosh
> use fishchat
> db.createUser({userPO: "fishchat", pwd: "your_password", roles: ["readWrite"]})
```

**Redis:**
默认配置即可，无需特殊设置。

#### 步骤 3：配置环境变量

复制环境变量模板：
```bash
cp .env .env
```

编辑 `.env` 文件，填入实际配置：
```bash
DB_URL=jdbc:mysql://localhost:3306/fish_chat?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

MONGODB_URI=mongodb://localhost:27017/fishchat

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

#### 步骤 4：构建项目

```bash
# 编译项目
mvn clean package -DskipTests

# 或者运行测试后打包
mvn clean package
```

#### 步骤 5：运行应用

```bash
# 方式 1：使用 Maven 运行
mvn spring-boot:run -pl fish-chat-bootstrap

# 方式 2：直接运行 jar 包
java -jar fish-chat-bootstrap/target/fish-chat-bootstrap-0.0.1-SNAPSHOT.jar

# 方式 3：指定环境变量运行
export DB_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
java -jar fish-chat-bootstrap/target/fish-chat-bootstrap-0.0.1-SNAPSHOT.jar
```

## 验证部署

### 1. 检查服务状态

```bash
# 检查应用是否启动成功
curl http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

应该返回 JSON 格式的错误信息（因为用户不存在），说明服务正常。

### 2. 使用测试页面

打开浏览器访问：http://localhost:8080/chat-test.html

1. 注册一个账号
2. 登录
3. 打开另一个浏览器窗口（或无痕模式）注册另一个账号
4. 两个窗口互相发送消息

### 3. 使用 Postman 或 curl 测试

**注册账号：**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "nickname": "Test User",
    "email": "test@example.com"
  }'
```

**登录：**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

响应示例：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "userId": 123456789,
    "username": "testuser",
    "nickname": "Test User",
    "avatarUrl": null
  }
}
```

## 常见问题排查

### Q1: Docker Compose 启动失败

**问题：** `Cannot start service app: ...`

**解决：**
1. 确保 Docker 和 Docker Compose 已安装并运行
2. 检查端口是否被占用：`lsof -i :8080`
3. 查看 Docker 日志：`docker-compose logs app`

### Q2: 数据库连接失败

**问题：** `Communications link failure`

**解决：**
1. 检查 MySQL 是否启动：`systemctl status mysql` 或 `docker ps | grep mysql`
2. 确认用户名密码正确
3. 检查防火墙设置

### Q3: MongoDB 认证失败

**问题：** `Authentication failed`

**解决：**
1. 确认 MongoDB URI 格式正确
2. 检查用户名密码是否正确创建
3. 确认 authentication-database 配置

### Q4: WebSocket 连接失败

**问题：** WebSocket 无法建立连接

**解决：**
1. 确保 Token 有效且未过期
2. 检查 CORS 配置
3. 确认防火墙允许 WebSocket 连接

### Q5: Maven 编译错误

**问题：** 编译时出现依赖错误

**解决：**
```bash
# 清理本地仓库缓存
rm -rf ~/.m2/repository/com/fish

# 重新下载依赖
mvn clean install -U
```

## 性能优化建议

### 生产环境配置

1. **调整 JVM 参数：**
```bash
java -Xms512m -Xmx2g -jar app.jar
```

2. **配置连接池：**
在 `application.yaml` 中调整 Druid 配置：
```yaml
spring:
  datasource:
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50
```

3. **启用 HTTPS：**
配置 SSL 证书，使用 WSS 代替 WS

4. **集群部署：**
使用 Nginx 做负载均衡，多实例部署应用

## 监控和维护

### 日志查看

```bash
# Docker 方式
docker-compose logs -f app

# 本地运行
tail -f logs/application.log
```

### 健康检查

添加健康检查端点（需配置 Spring Boot Actuator）：
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

访问：http://localhost:8080/actuator/health

### 数据备份

**MySQL:**
```bash
mysqldump -u root -p fish_chat > backup.sql
```

**MongoDB:**
```bash
mongodump --uri="mongodb://localhost:27017/fishchat" --out=/backup/mongodb
```

**Redis:**
```bash
redis-cli BGSAVE
```

## 升级指南

1. 停止服务
```bash
docker-compose down
```

2. 备份数据
```bash
docker-compose run --rm mysql mysqldump -u root -pfish_chat > backup.sql
```

3. 更新代码
```bash
git pull origin main
```

4. 重新构建
```bash
docker-compose build
```

5. 启动服务
```bash
docker-compose up -d
```

## 技术支持

如遇到问题，请：
1. 查看日志文件
2. 搜索项目 Issue
3. 提交新的 Issue 并附上错误日志
