# Fish-Chat API 文档

## 目录
- [认证接口](#认证接口)
  - [用户注册](#用户注册)
  - [用户登录](#用户登录)
  - [用户登出](#用户登出)
  
- [用户接口](#用户接口)
  - [获取用户信息](#获取用户信息)
  - [更新用户信息](#更新用户信息)
  
- [群组接口](#群组接口)
  - [创建群组](#创建群组)
  - [解散群组](#解散群组)
  - [邀请用户加入群组](#邀请用户加入群组)
  - [退出群组](#退出群组)
  - [获取群组成员列表](#获取群组成员列表)
  - [获取用户加入的群组列表](#获取用户加入的群组列表)
  - [更新群组信息](#更新群组信息)
  - [转让群主](#转让群主)
  - [踢出群组成员](#踢出群组成员)
  
- [聊天消息接口](#聊天消息接口)
  - [查询与指定用户的聊天记录](#查询与指定用户的聊天记录)
  - [查询当前用户发送的所有消息](#查询当前用户发送的所有消息)
  - [查询当前用户接收的所有消息](#查询当前用户接收的所有消息)
  - [查询与当前用户相关的所有消息](#查询与当前用户相关的所有消息)
  
- [消息接口](#消息接口)
  - [分页获取用户之间的聊天记录](#分页获取用户之间的聊天记录)
  - [分页获取群组消息](#分页获取群组消息)
  - [更新消息状态为已读](#更新消息状态为已读)
  
- [WebSocket接口](#websocket接口)
  - [获取在线用户数](#获取在线用户数)
  - [获取在线用户列表](#获取在线用户列表)
  - [更新群组成员缓存](#更新群组成员缓存)
  - [清除群组缓存](#清除群组缓存)
  - [WebSocket消息端点](#websocket消息端点)
  
---

## 认证接口

### 用户注册

**POST** `/auth/register`

注册新用户

#### 请求参数
```json
{
  "username": "string",  // 用户名
  "password": "string",  // 密码
  "mobile": "string",    // 手机号（可选）
  "email": "string",     // 邮箱（可选）
  "nickname": "string"   // 昵称（可选）
}
```

#### 响应结果
```json
{
  "code": 200,
  "msg": "注册成功",
  "data": null
}
```

### 用户登录

**POST** `/auth/login`

用户登录系统

#### 请求参数
```json
{
  "username": "string",  // 用户名
  "password": "string"   // 密码
}
```

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "string",     // 登录凭证
    "userId": 123,         // 用户ID
    "username": "string"   // 用户名
  }
}
```

### 用户登出

**POST** `/auth/logout`

用户登出系统

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "登出成功",
  "data": null
}
```

## 用户接口

### 获取用户信息

**GET** `/user/info`

获取当前登录用户的信息

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 123,
    "username": "string",
    "mobile": "string",
    "email": "string",
    "nickname": "string",
    "avatarUrl": "string",
    "profile": "string"
  }
}
```

### 更新用户信息

**POST** `/user/update`

更新当前用户的信息

#### 请求参数
```json
{
  "id": 123,          // 用户ID（必须与当前登录用户一致）
  "username": "string",
  "mobile": "string",
  "email": "string",
  "nickname": "string",
  "avatarUrl": "string",
  "profile": "string"
}
```

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

## 群组接口

### 创建群组

**POST** `/group/create`

创建一个新的群组

#### 请求参数
```json
{
  "name": "string",        // 群组名称
  "description": "string",  // 群组描述
  "avatar": "string"       // 群组头像（可选）
}
```

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": 123  // 群组ID
}
```

### 解散群组

**POST** `/group/dismiss/{groupId}`

解散指定的群组（仅群主可以操作）

#### 请求参数
- groupId: 群组ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "群组解散成功",
  "data": null
}
```

### 邀请用户加入群组

**POST** `/group/invite/{groupId}/{userId}`

邀请用户加入群组

#### 请求参数
- groupId: 群组ID（路径参数）
- userId: 被邀请用户ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "邀请成功",
  "data": null
}
```

### 退出群组

**POST** `/group/leave/{groupId}`

退出指定的群组

#### 请求参数
- groupId: 群组ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "退出群组成功",
  "data": null
}
```

### 获取群组成员列表

**GET** `/group/members/{groupId}`

获取指定群组的成员列表

#### 请求参数
- groupId: 群组ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 123,
      "groupId": 456,
      "userId": 789,
      "role": 1,      // 角色：1-普通成员, 2-管理员, 3-群主
      "status": 1     // 状态：1-正常, 0-已退出
    }
  ]
}
```

### 获取用户加入的群组列表

**GET** `/group/list`

获取当前用户加入的所有群组列表

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 123,
      "name": "string",
      "description": "string",
      "avatar": "string",
      "ownerId": 456,
      "status": 1
    }
  ]
}
```

### 更新群组信息

**POST** `/group/update/{groupId}`

更新群组信息

#### 请求参数
- groupId: 群组ID（路径参数）

```json
{
  "name": "string",        // 群组名称
  "description": "string",  // 群组描述
  "avatar": "string"       // 群组头像
}
```

#### 响应结果
```json
{
  "code": 200,
  "msg": "群组信息更新成功",
  "data": null
}
```

### 转让群主

**POST** `/group/transfer/{groupId}/{newOwnerId}`

将群主身份转让给其他成员

#### 请求参数
- groupId: 群组ID（路径参数）
- newOwnerId: 新群主ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "群主转让成功",
  "data": null
}
```

### 踢出群组成员

**POST** `/group/kick/{groupId}/{targetUserId}`

踢出群组成员（仅群主和管理员可以操作）

#### 请求参数
- groupId: 群组ID（路径参数）
- targetUserId: 被踢用户ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "成员踢出成功",
  "data": null
}
```

## 聊天消息接口

### 查询与指定用户的聊天记录

**GET** `/chat/messages/user/{userId}`

查询当前用户与指定用户的聊天记录

#### 请求参数
- userId: 对方用户ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "string",
      "from": "string",
      "to": "string",
      "content": "string",
      "timestamp": 1234567890,
      "status": "string"
    }
  ]
}
```

### 查询当前用户发送的所有消息

**GET** `/chat/messages/sent`

查询当前用户发送的所有消息

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "string",
      "from": "string",
      "to": "string",
      "content": "string",
      "timestamp": 1234567890,
      "status": "string"
    }
  ]
}
```

### 查询当前用户接收的所有消息

**GET** `/chat/messages/received`

查询当前用户接收的所有消息

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "string",
      "from": "string",
      "to": "string",
      "content": "string",
      "timestamp": 1234567890,
      "status": "string"
    }
  ]
}
```

### 查询与当前用户相关的所有消息

**GET** `/chat/messages/all`

查询与当前用户相关的所有消息（包括发送和接收）

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "string",
      "from": "string",
      "to": "string",
      "content": "string",
      "timestamp": 1234567890,
      "status": "string"
    }
  ]
}
```

## 消息接口

### 分页获取用户之间的聊天记录

**GET** `/messages/user/{userId}`

分页获取当前用户与指定用户之间的聊天记录

#### 请求参数
- userId: 对方用户ID（路径参数）
- page: 页码（默认1，查询参数）
- size: 每页大小（默认20，最大100，查询参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "string",
      "from": "string",
      "to": "string",
      "content": "string",
      "timestamp": 1234567890,
      "status": "string"
    }
  ]
}
```

### 分页获取群组消息

**GET** `/messages/group/{groupId}`

分页获取指定群组的消息

#### 请求参数
- groupId: 群组ID（路径参数）
- page: 页码（默认1，查询参数）
- size: 每页大小（默认20，最大100，查询参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": "string",
      "from": "string",
      "groupId": "string",
      "content": "string",
      "timestamp": 1234567890,
      "status": "string"
    }
  ]
}
```

### 更新消息状态为已读

**POST** `/messages/read/{messageId}`

将指定消息标记为已读

#### 请求参数
- messageId: 消息ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "消息状态更新成功",
  "data": null
}
```

## WebSocket接口

### 获取在线用户数

**GET** `/api/websocket/online/count`

获取当前在线用户数

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": 10  // 在线用户数
}
```

### 获取在线用户列表

**GET** `/api/websocket/online/users`

获取当前在线用户列表

#### 请求参数
无

#### 响应结果
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    "userId1",
    "userId2",
    "userId3"
  ]
}
```

### 更新群组成员缓存

**POST** `/api/websocket/group/{groupId}/cache/update`

更新指定群组的成员缓存

#### 请求参数
- groupId: 群组ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "群组成员缓存更新成功",
  "data": null
}
```

### 清除群组缓存

**DELETE** `/api/websocket/group/{groupId}/cache`

清除指定群组的成员缓存

#### 请求参数
- groupId: 群组ID（路径参数）

#### 响应结果
```json
{
  "code": 200,
  "msg": "群组成员缓存清除成功",
  "data": null
}
```

### WebSocket消息端点

**WebSocket** `/ws`

WebSocket连接端点，用于实时消息通信

#### 连接参数
- token: 用户登录凭证

#### 客户端发送消息的地址前缀
- `/app/chat` - 发送私聊消息
- `/app/group` - 发送群组消息
- `/app/ping` - 发送心跳消息

#### 客户端订阅消息的地址
- `/user/queue/messages` - 接收私聊消息
- `/user/queue/errors` - 接收错误消息
- `/topic/group/{groupId}` - 订阅群组消息
- `/user/queue/pong` - 接收心跳响应

#### 私聊消息格式
```json
{
  "from": "发送方用户ID",
  "to": "接收方用户ID",
  "content": "消息内容"
}
```

#### 群组消息格式
```json
{
  "from": "发送方用户ID",
  "groupId": "群组ID",
  "content": "消息内容"
}
```

#### 心跳消息格式
```json
{
  "from": "用户ID"
}
```