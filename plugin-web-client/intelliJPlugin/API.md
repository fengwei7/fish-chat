# Fish-Chat 接口文档

## 1. 通用约定

### 1.1 接口前缀

所有 HTTP REST API 统一以 `/api` 为前缀（如果配置了 Servlet context-path），具体以前端实际配置为准。

### 1.2 统一响应格式

所有 REST API 返回统一包装结果：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1715567890123
}
```

| 字段      | 类型    | 说明                    |
|-----------|---------|-------------------------|
| code      | Integer | 状态码，200 表示成功    |
| message   | String  | 提示信息                |
| data      | Object  | 业务数据                |
| timestamp | Long    | 时间戳（毫秒）          |

### 1.3 分页响应格式

分页接口返回 PageResult 包装：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "data": [],
    "pageNum": 0,
    "pageSize": 20,
    "total": 100
  },
  "timestamp": 1715567890123
}
```

| 字段     | 类型   | 说明              |
|----------|--------|-------------------|
| data     | Array  | 数据列表          |
| pageNum  | Long   | 当前页码（从0开始）|
| pageSize | Long   | 每页数量          |
| total    | Long   | 总记录数          |

### 1.4 认证方式

REST API 使用 Sa-Token 进行登录态校验，请求头需携带：

```
fish-token: <token>
```

WebSocket 连接通过握手阶段 URL 参数或 Cookie 携带 token 进行认证。

---

## 2. 认证接口

### 2.1 用户注册

- **接口**: `POST /auth/register`
- **说明**: 用户注册

**请求参数：**

| 字段     | 类型   | 必填 | 说明                |
|----------|--------|------|---------------------|
| username | String | 是   | 用户名（3-20字符）  |
| password | String | 是   | 密码（6-32字符）    |
| email    | String | 否   | 邮箱                |
| mobile   | String | 否   | 手机号              |
| nickname | String | 否   | 昵称                |

**响应数据**：`Boolean` — 注册是否成功

---

### 2.2 用户登录

- **接口**: `POST /auth/login`
- **说明**: 用户登录

**请求参数：**

| 字段     | 类型   | 必填 | 说明   |
|----------|--------|------|--------|
| username | String | 是   | 用户名 |
| password | String | 是   | 密码   |

**响应数据** — AuthDTO：

| 字段      | 类型   | 说明      |
|-----------|--------|-----------|
| token     | String | Token     |
| code      | String | 用户 code |
| username  | String | 用户名    |
| nickname  | String | 昵称      |
| avatarUrl | String | 头像 URL  |

---

### 2.3 用户登出

- **接口**: `POST /auth/logout`
- **说明**: 用户登出

**响应数据**：`null`

---

## 3. 用户接口

### 3.1 查看当前用户信息

- **接口**: `GET /user/profile`
- **说明**: 查看当前登录用户信息

**响应数据** — UserDTO：

| 字段      | 类型    | 说明         |
|-----------|---------|--------------|
| code      | String  | 用户 code    |
| username  | String  | 用户名       |
| nickname  | String  | 昵称         |
| avatarUrl | String  | 头像 URL     |
| profile   | String  | 个人简介     |
| email     | String  | 邮箱         |
| mobile    | String  | 手机号       |
| online    | Boolean | 是否在线     |

---

### 3.2 修改当前用户信息

- **接口**: `POST /user/profile`
- **说明**: 修改当前登录用户信息

**请求参数：**

| 字段      | 类型   | 必填 | 说明                   |
|-----------|--------|------|------------------------|
| nickname  | String | 否   | 昵称（1-30字符）       |
| avatarUrl | String | 否   | 头像URL（最大100字符） |
| profile   | String | 否   | 个人简介（最大255字符）|
| email     | String | 否   | 邮箱                   |
| mobile    | String | 否   | 手机号（最大20字符）   |

**响应数据** — UserDTO（同 3.1）

---

### 3.3 根据用户 code 查看用户信息

- **接口**: `GET /user/{code}`
- **说明**: 根据用户 code 查看指定用户信息

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 用户 code |

**响应数据** — UserDTO（同 3.1）

---

### 3.4 获取在线用户列表

- **接口**: `GET /user/online`
- **说明**: 获取当前在线用户列表（分页）

**查询参数：**

| 字段     | 类型 | 必填 | 默认值 | 说明   |
|----------|------|------|--------|--------|
| pageNum  | int  | 否   | 0      | 页码   |
| pageSize | int  | 否   | 20     | 每页数 |

**响应数据** — `PageResult<String>`：在线用户 code 列表

---

### 3.5 搜索用户

- **接口**: `GET /user/search`
- **说明**: 按用户名/昵称搜索用户

**查询参数：**

| 字段     | 类型   | 必填 | 默认值 | 说明       |
|----------|--------|------|--------|------------|
| keyword  | String | 是   | —      | 搜索关键字 |
| pageNum  | int    | 否   | 0      | 页码       |
| pageSize | int    | 否   | 20     | 每页数     |

**响应数据** — `PageResult<UserDTO>`

---

## 4. 好友接口

### 4.1 添加好友

- **接口**: `POST /friends`
- **说明**: 发送好友申请

**请求参数：**

| 字段       | 类型   | 必填 | 说明         |
|------------|--------|------|--------------|
| friendCode | String | 是   | 对方用户 code |
| remark     | String | 否   | 备注         |

**响应数据**：`null`

---

### 4.2 接受好友请求

- **接口**: `POST /friends/accept`
- **说明**: 接受好友申请

**请求参数：**

| 字段       | 类型   | 必填 | 说明         |
|------------|--------|------|--------------|
| friendCode | String | 是   | 对方用户 code |

**响应数据**：`null`

---

### 4.3 删除好友

- **接口**: `POST /friends/remove`
- **说明**: 删除好友

**请求参数：**

| 字段       | 类型   | 必填 | 说明         |
|------------|--------|------|--------------|
| friendCode | String | 是   | 对方用户 code |

**响应数据**：`null`

---

### 4.4 好友列表

- **接口**: `GET /friends`
- **说明**: 获取我的好友列表（分页）

**查询参数：**

| 字段     | 类型 | 必填 | 默认值 | 说明   |
|----------|------|------|--------|--------|
| pageNum  | int  | 否   | 0      | 页码   |
| pageSize | int  | 否   | 20     | 每页数 |

**响应数据** — `PageResult<FriendDTO>`：

| 字段      | 类型    | 说明      |
|-----------|---------|-----------|
| code      | String  | 用户 code |
| username  | String  | 用户名    |
| nickname  | String  | 昵称      |
| avatarUrl | String  | 头像 URL  |
| remark    | String  | 备注      |
| status    | Integer | 状态      |
| online    | Boolean | 是否在线  |

---

### 4.5 好友申请列表

- **接口**: `GET /friends/requests`
- **说明**: 获取收到的好友申请列表（分页）

**查询参数：**

| 字段     | 类型 | 必填 | 默认值 | 说明   |
|----------|------|------|--------|--------|
| pageNum  | int  | 否   | 0      | 页码   |
| pageSize | int  | 否   | 20     | 每页数 |

**响应数据** — `PageResult<FriendDTO>`（同 4.4）

---

### 4.6 搜索用户

- **接口**: `GET /friends/search`
- **说明**: 搜索用户（可用于添加好友前查找）

**查询参数：**

| 字段     | 类型   | 必填 | 默认值 | 说明       |
|----------|--------|------|--------|------------|
| keyword  | String | 是   | —      | 搜索关键字 |
| pageNum  | int    | 否   | 0      | 页码       |
| pageSize | int    | 否   | 20     | 每页数     |

**响应数据** — `PageResult<FriendDTO>`（同 4.4）

---

## 5. 群组接口

### 5.1 创建群组

- **接口**: `POST /groups`
- **说明**: 创建新群组

**请求参数：**

| 字段   | 类型   | 必填 | 说明      |
|--------|--------|------|-----------|
| name   | String | 是   | 群名称    |
| avatar | String | 否   | 群头像URL |

**响应数据** — GroupDTO：

| 字段         | 类型    | 说明          |
|--------------|---------|---------------|
| code         | String  | 群组 code     |
| name         | String  | 群名称        |
| avatar       | String  | 群头像        |
| ownerCode    | String  | 群主用户 code |
| notice       | String  | 群公告        |
| maxMembers   | Integer | 最大成员数    |
| memberCount  | Integer | 当前成员数    |
| status       | Integer | 状态          |

---

### 5.2 获取群组信息

- **接口**: `GET /groups/{code}`
- **说明**: 获取指定群组信息

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 群组 code |

**响应数据** — GroupDTO（同 5.1）

---

### 5.3 解散群组

- **接口**: `POST /groups/{code}/dismiss`
- **说明**: 解散指定群组

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 群组 code |

**响应数据**：`null`

---

### 5.4 添加成员

- **接口**: `POST /groups/{code}/members`
- **说明**: 向群组添加成员

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 群组 code |

**请求参数：**

| 字段     | 类型   | 必填 | 说明          |
|----------|--------|------|---------------|
| userCode | String | 是   | 要添加的用户 code |

**响应数据**：`null`

---

### 5.5 移除成员

- **接口**: `POST /groups/{code}/members/remove`
- **说明**: 从群组移除成员

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 群组 code |

**请求参数：**

| 字段     | 类型   | 必填 | 说明          |
|----------|--------|------|---------------|
| userCode | String | 是   | 要移除的用户 code |

**响应数据**：`null`

---

### 5.6 我的群组列表

- **接口**: `GET /groups/my`
- **说明**: 获取我加入的群组列表（分页）

**查询参数：**

| 字段     | 类型 | 必填 | 默认值 | 说明   |
|----------|------|------|--------|--------|
| pageNum  | int  | 否   | 0      | 页码   |
| pageSize | int  | 否   | 20     | 每页数 |

**响应数据** — `PageResult<GroupDTO>`（同 5.1）

---

### 5.7 搜索群组

- **接口**: `GET /groups/search`
- **说明**: 搜索群组

**查询参数：**

| 字段     | 类型   | 必填 | 默认值 | 说明       |
|----------|--------|------|--------|------------|
| keyword  | String | 是   | —      | 搜索关键字 |
| pageNum  | int    | 否   | 0      | 页码       |
| pageSize | int    | 否   | 20     | 每页数     |

**响应数据** — `PageResult<GroupDTO>`（同 5.1）

---

## 6. 频道接口

### 6.1 创建频道

- **接口**: `POST /channels`
- **说明**: 创建新频道

**请求参数：**

| 字段        | 类型   | 必填 | 说明        |
|-------------|--------|------|-------------|
| name        | String | 是   | 频道名称    |
| avatar      | String | 否   | 频道头像URL |
| description | String | 否   | 频道描述    |

**响应数据** — ChannelDTO：

| 字段             | 类型    | 说明           |
|------------------|---------|----------------|
| code             | String  | 频道 code      |
| name             | String  | 频道名称       |
| avatar           | String  | 频道头像       |
| ownerCode        | String  | 创建者用户 code |
| description      | String  | 频道描述       |
| subscriberCount  | Integer | 订阅人数       |
| status           | Integer | 状态           |

---

### 6.2 获取频道信息

- **接口**: `GET /channels/{code}`
- **说明**: 获取指定频道信息

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 频道 code |

**响应数据** — ChannelDTO（同 6.1）

---

### 6.3 订阅频道

- **接口**: `POST /channels/{code}/subscribe`
- **说明**: 订阅指定频道

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 频道 code |

**响应数据**：`null`

---

### 6.4 取消订阅

- **接口**: `POST /channels/{code}/unsubscribe`
- **说明**: 取消订阅指定频道

**路径参数：**

| 字段 | 类型   | 必填 | 说明      |
|------|--------|------|-----------|
| code | String | 是   | 频道 code |

**响应数据**：`null`

---

### 6.5 我的频道列表

- **接口**: `GET /channels/my`
- **说明**: 获取我订阅的频道列表（分页）

**查询参数：**

| 字段     | 类型 | 必填 | 默认值 | 说明   |
|----------|------|------|--------|--------|
| pageNum  | int  | 否   | 0      | 页码   |
| pageSize | int  | 否   | 20     | 每页数 |

**响应数据** — `PageResult<ChannelDTO>`（同 6.1）

---

### 6.6 搜索频道

- **接口**: `GET /channels/search`
- **说明**: 搜索频道

**查询参数：**

| 字段     | 类型   | 必填 | 默认值 | 说明       |
|----------|--------|------|--------|------------|
| keyword  | String | 是   | —      | 搜索关键字 |
| pageNum  | int    | 否   | 0      | 页码       |
| pageSize | int    | 否   | 20     | 每页数     |

**响应数据** — `PageResult<ChannelDTO>`（同 6.1）

---

## 7. 消息接口

### 7.1 获取房间历史消息

- **接口**: `GET /messages/{roomCode}`
- **说明**: 分页拉取指定房间的历史消息

**路径参数：**

| 字段     | 类型   | 必填 | 说明      |
|----------|--------|------|-----------|
| roomCode | String | 是   | 房间 code |

**查询参数：**

| 字段 | 类型 | 必填 | 默认值 | 说明   |
|------|------|------|--------|--------|
| page | int  | 否   | 0      | 页码   |
| size | int  | 否   | 20     | 每页数 |

**响应数据**：

```json
{
  "messages": [],    // ChatMessageDTO 列表
  "page": 0,
  "size": 20,
  "total": 100
}
```

**ChatMessageDTO 字段：**

| 字段         | 类型   | 说明                  |
|--------------|--------|-----------------------|
| id           | String | 消息ID                |
| type         | String | 消息类型              |
| from         | String | 发送者用户 code        |
| senderName   | String | 发送者昵称            |
| senderAvatar | String | 发送者头像            |
| to           | String | 接收方/房间 code       |
| content      | String | 消息内容              |
| timestamp    | Long   | 时间戳（毫秒）        |
| roomCode     | String | 房间 code             |
| roomType     | String | 房间类型              |
| fileName     | String | 文件名（文件消息时）  |
| fileSize     | Long   | 文件大小（字节）      |

---

### 7.2 同步断线消息

- **接口**: `GET /messages/{roomCode}/sync`
- **说明**: 同步指定时间点之后的消息（断线重连用）

**路径参数：**

| 字段     | 类型   | 必填 | 说明      |
|----------|--------|------|-----------|
| roomCode | String | 是   | 房间 code |

**查询参数：**

| 字段  | 类型 | 必填 | 说明               |
|-------|------|------|--------------------|
| after | long | 是   | 时间戳（毫秒），拉取该时间之后的消息 |

**响应数据** — `List<ChatMessageDTO>`（同 7.1）

---

## 8. 文件接口

### 8.1 上传文件

- **接口**: `POST /file/upload`
- **说明**: 上传文件
- **Content-Type**: `multipart/form-data`

**请求参数：**

| 字段 | 类型   | 必填 | 说明   |
|------|--------|------|--------|
| file | File   | 是   | 文件   |

**响应数据**：

```json
{
  "fileName": "xxx.png",
  "accessUrl": "/uploads/xxx.png"
}
```

| 字段      | 类型   | 说明        |
|-----------|--------|-------------|
| fileName  | String | 文件名      |
| accessUrl | String | 访问URL     |

---

### 8.2 下载文件

- **接口**: `GET /file/download/{fileName}`
- **说明**: 下载文件

**路径参数：**

| 字段     | 类型   | 必填 | 说明   |
|----------|--------|------|--------|
| fileName | String | 是   | 文件名 |

**响应**：文件流（`application/octet-stream`）

---

## 9. WebSocket 消息协议

### 9.1 连接地址

```
ws://<host>:<port>/ws
```

> 连接前需通过 Sa-Token 认证，握手阶段携带 token。

### 9.2 消息包格式

WebSocket 采用 JSON 文本帧通信，统一消息包结构：

```json
{
  "cmd": "MSG",
  "code": 0,
  "reqCode": "uuid",
  "body": {
    "msgId": "xxx",
    "senderCode": "u1",
    "senderName": "用户1",
    "senderAvatar": "http://...",
    "roomCode": "group:xxx",
    "roomType": "GROUP",
    "msgType": "TEXT",
    "content": "hello",
    "fileName": null,
    "fileSize": null,
    "timestamp": 1715567890123,
    "extra": {}
  }
}
```

**消息包字段：**

| 字段    | 类型   | 说明                                              |
|---------|--------|---------------------------------------------------|
| cmd     | String | 命令：MSG / HEARTBEAT / SYNC / ACK / ERROR / NOTIFY |
| code    | Integer| 状态码，0=成功                                    |
| reqCode | String | 请求追踪ID（客户端生成，服务端回显）              |
| body    | Object | 消息体                                            |

**Body 字段：**

| 字段         | 类型   | 说明                                                  |
|--------------|--------|-------------------------------------------------------|
| msgId        | String | 消息ID（MongoDB _id，服务端下发）                      |
| senderCode   | String | 发送者用户 code                                        |
| senderName   | String | 发送者名称                                            |
| senderAvatar | String | 发送者头像                                            |
| roomCode     | String | 目标房间ID                                            |
| roomType     | String | 房间类型：PRIVATE / GROUP / CHANNEL                    |
| msgType      | String | 消息类型：TEXT / IMAGE / FILE / SYSTEM                 |
| content      | String | 消息内容（TEXT 为文本，IMAGE/FILE 为文件URL）           |
| fileName     | String | 文件名（IMAGE / FILE 时）                              |
| fileSize     | Long   | 文件大小（字节）                                      |
| timestamp    | Long   | 时间戳（毫秒）                                        |
| extra        | Map    | 扩展字段（如 targetId 等）                             |

### 9.3 房间 code 规则

| 类型    | 格式                        | 示例                   |
|---------|-----------------------------|------------------------|
| 私聊    | `private:<user1>:<user2>`   | `private:u1:u2`        |
| 群聊    | `group:<groupCode>`         | `group:g123`           |
| 频道    | `channel:<channelCode>`     | `channel:c456`         |

### 9.4 支持的命令

#### 9.4.1 MSG — 发送消息

**上行示例（客户端→服务端）：**

```json
{
  "cmd": "MSG",
  "reqCode": "req-123",
  "body": {
    "roomCode": "group:g123",
    "roomType": "GROUP",
    "msgType": "TEXT",
    "content": "大家好"
  }
}
```

**下行示例（服务端→客户端广播）：**

```json
{
  "cmd": "MSG",
  "code": 0,
  "reqCode": "req-123",
  "body": {
    "msgId": "664xxx",
    "senderCode": "u1",
    "senderName": "用户1",
    "senderAvatar": "http://...",
    "roomCode": "group:g123",
    "roomType": "GROUP",
    "msgType": "TEXT",
    "content": "大家好",
    "timestamp": 1715567890123
  }
}
```

**ACK 响应（单独下发给发送者）：**

```json
{
  "cmd": "ACK",
  "code": 0,
  "reqCode": "req-123"
}
```

---

#### 9.4.2 HEARTBEAT — 心跳

**上行示例：**

```json
{
  "cmd": "HEARTBEAT"
}
```

**下行示例：**

```json
{
  "cmd": "HEARTBEAT",
  "code": 0
}
```

> 服务端在收到心跳后会刷新用户的 Redis 在线状态。

---

#### 9.4.3 SYNC — 消息同步

断线重连后，客户端发送 SYNC 命令，服务端会通知各房间同步就绪。

**上行示例：**

```json
{
  "cmd": "SYNC",
  "reqCode": "req-sync-1",
  "body": {
    "extra": {
      "lastTimestamp": 1715567800000
    }
  }
}
```

**下行示例（服务端通知各房间同步就绪）：**

```json
{
  "cmd": "NOTIFY",
  "code": 0,
  "body": {
    "msgType": "SYNC-READY",
    "content": "房间 group:g123 同步就绪，请拉取历史消息",
    "timestamp": 1715567890123
  }
}
```

**ACK 响应：**

```json
{
  "cmd": "ACK",
  "code": 0,
  "reqCode": "req-sync-1"
}
```

> 客户端收到 `SYNC-READY` 通知后，应调用 REST API `GET /messages/{roomCode}/sync?after={lastTimestamp}` 拉取历史消息。

---

#### 9.4.4 ERROR — 错误通知

当消息处理失败时，服务端会下发 ERROR 包：

```json
{
  "cmd": "ERROR",
  "code": 1,
  "reqCode": "req-123",
  "body": {
    "content": "房间不存在或无权访问"
  }
}
```

---

#### 9.4.5 NOTIFY — 系统通知

服务端主动推送的系统通知：

```json
{
  "cmd": "NOTIFY",
  "code": 0,
  "body": {
    "msgType": "SYSTEM",
    "content": "您的账号在其他设备登录",
    "timestamp": 1715567890123
  }
}
```

---

### 9.5 消息类型枚举

| 枚举值 | 说明       |
|--------|------------|
| TEXT   | 文本消息   |
| IMAGE  | 图片消息   |
| FILE   | 文件消息   |
| SYSTEM | 系统消息   |

### 9.6 房间类型枚举

| 枚举值  | 说明             |
|---------|------------------|
| PRIVATE | 单聊（1v1）      |
| GROUP   | 群聊             |
| CHANNEL | 频道（一对多广播）|

---

## 10. 状态码说明

| 状态码 | 说明             |
|--------|------------------|
| 200    | 请求成功         |
| 401    | 未认证/登录态失效 |
| 403    | 无权访问         |
| 404    | 资源不存在       |
| 500    | 服务器内部错误   |

---

## 11. 接口汇总表

### REST API 汇总

| 方法   | 路径                                | 说明              |
|--------|-------------------------------------|-------------------|
| POST   | /auth/register                      | 用户注册          |
| POST   | /auth/login                         | 用户登录          |
| POST   | /auth/logout                        | 用户登出          |
| GET    | /user/profile                       | 查看当前用户信息  |
| POST   | /user/profile                       | 修改当前用户信息  |
| GET    | /user/{code}                        | 根据code查看用户  |
| GET    | /user/online                        | 在线用户列表      |
| GET    | /user/search                        | 搜索用户          |
| POST   | /friends                            | 添加好友          |
| POST   | /friends/accept                     | 接受好友请求      |
| POST   | /friends/remove                     | 删除好友          |
| GET    | /friends                            | 好友列表          |
| GET    | /friends/requests                   | 好友申请列表      |
| GET    | /friends/search                     | 搜索用户          |
| POST   | /groups                             | 创建群组          |
| GET    | /groups/{code}                      | 获取群组信息      |
| POST   | /groups/{code}/dismiss              | 解散群组          |
| POST   | /groups/{code}/members              | 添加成员          |
| POST   | /groups/{code}/members/remove       | 移除成员          |
| GET    | /groups/my                          | 我的群组列表      |
| GET    | /groups/search                      | 搜索群组          |
| POST   | /channels                           | 创建频道          |
| GET    | /channels/{code}                    | 获取频道信息      |
| POST   | /channels/{code}/subscribe          | 订阅频道          |
| POST   | /channels/{code}/unsubscribe        | 取消订阅          |
| GET    | /channels/my                        | 我的频道列表      |
| GET    | /channels/search                    | 搜索频道          |
| GET    | /messages/{roomCode}                | 房间历史消息      |
| GET    | /messages/{roomCode}/sync           | 同步断线消息      |
| POST   | /file/upload                        | 上传文件          |
| GET    | /file/download/{fileName}           | 下载文件          |

### WebSocket 命令汇总

| 命令      | 方向   | 说明           |
|-----------|--------|----------------|
| MSG       | 双向   | 发送/接收消息  |
| HEARTBEAT | 双向   | 心跳           |
| SYNC      | 上行   | 断线消息同步   |
| ACK       | 下行   | 确认响应       |
| ERROR     | 下行   | 错误通知       |
| NOTIFY    | 下行   | 系统通知       |
