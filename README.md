# fish-chat
```text
 _____                _    ______ _     _     
|_   _|              | |   |  ___(_)   | |    
  | | ___  _   _  ___| |__ | |_   _ ___| |__  
  | |/ _ \| | | |/ __| '_ \|  _| | / __| '_ \ 
  | | (_) | |_| | (__| | | | |   | \__ \ | | |
  \_/\___/ \__,_|\___|_| |_\_|   |_|___/_| |_|
                                              
                                              
```
## 介绍

fish-chat 是一个基于 Spring Boot 的聊天服务项目，旨在提供一个基础的即时通讯功能实现。该项目采用了现代化的技术栈，包括 MySQL、Redis、MongoDB 等，支持权限认证、缓存、数据持久化等功能。

## 软件架构

### 技术选型

- 后端框架：Spring Boot 2.6.13
- 数据库：MySQL 8.0、MongoDB
- 缓存：Redis
- 权限框架：Sa-Token 1.44.0
- ORM 框架：MyBatis-Plus 3.5.13
- 连接池：Druid 1.2.8
- 构建工具：Maven 3.8.1+
- 其他工具：Lombok、Hutool、Fastjson

### 架构特性

- 权限认证（Sa-Token）
- Redis 缓存支持
- MongoDB 数据存储
- MySQL 数据持久化
- 响应式编程支持
- 数据源监控（Druid）

## 环境要求

- JDK 1.8 或更高版本
- Maven 3.8.1+
- MySQL 8.0+
- Redis
- MongoDB
- Docker (可选，用于容器化部署)

## 安装教程

1.  确保已安装 JDK 1.8+、Maven、MySQL、Redis 和 MongoDB
2.  配置数据库连接信息（在 application.yaml 中）
3.  使用 Maven 构建项目：`mvn clean package`
4.  运行项目：`java -jar bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar`

#### 使用说明

### 1. MySQL 服务

```bash
docker run -d \
  --name fish-chat-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=test \
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql \
  mysql:8.0
```


### 2. Redis 服务

```bash
docker run -d \
  --name fish-chat-redis \
  -p 6379:6379 \
  redis:latest \
  redis-server --requirepass 123456
```


### 3. MongoDB 服务

```bash
docker run -d \
  --name fish-chat-mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=myuser \
  -e MONGO_INITDB_ROOT_PASSWORD=mypassword \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:latest
```


### 4. fish-chat 应用服务

首先需要构建 Docker 镜像：

```bash
docker build -t fish-chat -f docker/Dockerfile .
```


然后运行应用容器：

```bash
docker run -d \
  --name fish-chat-app \
  --link fish-chat-mysql:mysql \
  --link fish-chat-redis:redis \
  --link fish-chat-mongodb:mongodb \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  fish-chat
```


### 启动顺序说明

由于应用服务依赖于其他三个数据库服务，您需要按以下顺序启动容器：

1. 首先启动 MySQL:
   ```bash
   docker run -d --name fish-chat-mysql -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=test -p 3306:3306 -v mysql_data:/var/lib/mysql mysql:8.0
   ```


2. 然后启动 Redis:
   ```bash
   docker run -d --name fish-chat-redis -p 6379:6379 redis:latest redis-server --requirepass 123456
   ```


3. 接着启动 MongoDB:
   ```bash
   docker run -d --name fish-chat-mongodb -e MONGO_INITDB_ROOT_USERNAME=myuser -e MONGO_INITDB_ROOT_PASSWORD=mypassword -p 27017:27017 -v mongodb_data:/data/db mongo:latest
   ```


4. 最后启动主应用 (需要等待数据库服务完全启动):
   ```bash
   docker run -d --name fish-chat-app --link fish-chat-mysql:mysql --link fish-chat-redis:redis --link fish-chat-mongodb:mongodb -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker fish-chat
   ```


### 停止和清理命令

停止所有容器:
```bash
docker stop fish-chat-app fish-chat-mysql fish-chat-redis fish-chat-mongodb
```


删除所有容器:
```bash
docker rm fish-chat-app fish-chat-mysql fish-chat-redis fish-chat-mongodb
```


删除数据卷:
```bash
docker volume rm mysql_data mongodb_data
```


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

