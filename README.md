# fish-chat

#### 介绍
fish-chat.....

#### 软件架构
软件架构说明


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

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


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
