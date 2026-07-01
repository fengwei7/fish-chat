# User 模块 DDD 架构重构说明

## 📋 架构总览

本次重构将 User 模块按照 DDD 四层架构进行了完整重构，明确了各层的职责和边界。

### 架构分层

```
┌─────────────────────────────────────────────────────────┐
│                  Interfaces 层                           │
│  - UserController (接收 HTTP 请求)                       │
│  - Request/Response DTO (参数校验)                       │
│  - UserFeignClient (OpenFeign 接口，预留)                │
└────────────────────┬────────────────────────────────────┘
                     │ 调用
┌────────────────────▼────────────────────────────────────┐
│                  Application 层                          │
│  - UserAppService (事务管理 + DTO 转换 + 业务编排)       │
│  - UserDomainServiceImpl (领域服务实现)                  │
└────────────────────┬────────────────────────────────────┘
                     │ 调用
┌────────────────────▼────────────────────────────────────┐
│                  Domain 层                               │
│  - User Entity (充血模型，包含业务行为)                  │
│  - UserDomainService (领域服务接口)                      │
│  - UserRepository (仓储接口)                             │
└────────────────────┬────────────────────────────────────┘
                     │ 实现
┌────────────────────▼────────────────────────────────────┐
│                  Infrastructure 层                       │
│  - UserRepositoryImpl (仓储实现)                         │
│  - UserConverter (Entity ↔ PO 转换)                     │
│  - UserMapper (MyBatis-Plus Mapper)                     │
│  - UserPO (持久化对象)                                   │
└─────────────────────────────────────────────────────────┘
```

## 🎯 各层职责

### 1. Interfaces 层（接口层）

**职责**：
- 接收 HTTP 请求
- 参数校验（JSR-303）
- 调用 AppService
- 返回统一格式的响应

**文件清单**：
```
interfaces/src/main/java/com/fish/chat/interfaces/user/
├── controller/
│   └── UserController.java          # REST API 控制器
├── request/
│   ├── UserRegisterRequest.java     # 注册请求 DTO
│   ├── UserLoginRequest.java        # 登录请求 DTO
│   ├── UserUpdateRequest.java       # 更新资料请求 DTO
│   └── ChangePasswordRequest.java   # 修改密码请求 DTO
└── feign/
    └── UserFeignClient.java         # OpenFeign 接口（预留）
```

**设计原则**：
- ✅ 薄 Controller：不包含业务逻辑
- ✅ 使用 `@Valid` 进行参数校验
- ✅ 使用 `@RequestAttribute` 获取认证信息
- ✅ 统一返回 `Result<T>` 格式

### 2. Application 层（应用层）

**职责**：
- 事务管理（`@Transactional`）
- DTO 转换（Entity ↔ DTO）
- 业务编排（调用 DomainService）

**文件清单**：
```
application/src/main/java/com/fish/chat/application/user/
├── dto/
│   └── UserDTO.java                 # 用户数据传输对象
├── service/
│   ├── UserAppService.java          # 应用服务接口
│   └── impl/
│       ├── UserAppServiceImpl.java  # 应用服务实现
│       └── UserDomainServiceImpl.java # 领域服务实现
```

**设计原则**：
- ✅ 事务边界：所有写操作都在 `@Transactional` 中
- ✅ 薄 AppService：不包含业务逻辑，只编排 DomainService
- ✅ DTO 转换：负责 Entity 和 DTO 之间的转换
- ✅ 日志记录：记录关键操作日志

### 3. Domain 层（领域层）

**职责**：
- 定义领域模型（Entity）
- 定义领域服务接口
- 定义仓储接口
- 包含业务规则和验证逻辑

**文件清单**：
```
domain/src/main/java/com/fish/chat/domain/user/
├── model/entity/
│   └── User.java                    # 用户实体（充血模型）
├── repository/
│   └── UserRepository.java          # 用户仓储接口
└── service/
    └── UserDomainService.java       # 用户领域服务接口
```

**设计原则**：
- ✅ 充血模型：Entity 包含业务行为（updateProfile、changePassword）
- ✅ 依赖倒置：Domain 层定义接口，Infrastructure 层实现
- ✅ 纯业务逻辑：不包含框架依赖（无 Spring、MyBatis 注解）
- ✅ 领域规则：业务验证逻辑在 Entity 中执行

### 4. Infrastructure 层（基础设施层）

**职责**：
- 实现仓储接口
- 数据库操作（MyBatis-Plus）
- PO ↔ Entity 转换（MapStruct）

**文件清单**：
```
infrastructure/src/main/java/com/fish/chat/infrastructure/persistence/
├── base/
│   ├── BaseRepositoryImpl.java      # 基础仓储实现
│   └── MyBaseMapper.java            # 基础 Mapper 接口
├── repository/
│   └── UserRepositoryImpl.java      # 用户仓储实现
├── converter/
│   ├── BaseConverter.java           # 基础转换器接口
│   └── UserConverter.java           # 用户转换器
├── mapper/
│   └── UserMapper.java              # 用户 Mapper
└── po/
    └── UserPO.java                  # 用户持久化对象
```

**设计原则**：
- ✅ 泛型编程：通过泛型保证类型安全
- ✅ MapStruct：编译期生成转换代码，性能优于手写
- ✅ 职责单一：只负责数据访问和转换

## 🔄 调用流程示例

### 示例 1：用户注册

```
1. UserController.register()
   ↓ 接收 UserRegisterRequest
   ↓ 参数校验（@Valid）
   
2. UserAppService.register()
   ↓ @Transactional 开启事务
   ↓ 调用 userDomainService.createUser()
   
3. UserDomainService.createUser()
   ↓ 检查用户名是否存在
   ↓ 调用 User.create() 工厂方法（密码加密）
   ↓ 调用 userRepository.save()
   
4. UserRepositoryImpl.save()
   ↓ 转换 Entity → PO
   ↓ 调用 UserMapper.insert()
   ↓ 回填 ID
   
5. UserAppService.register()
   ↓ 转换 Entity → DTO
   ↓ 提交事务
   ↓ 返回 UserDTO
```

### 示例 2：更新用户资料

```
1. UserController.updateProfile()
   ↓ 接收 @RequestAttribute("userCode")
   ↓ 接收 UserUpdateRequest
   ↓ 参数校验（@Valid）
   
2. UserAppService.updateCurrentUser()
   ↓ @Transactional 开启事务
   ↓ 调用 userDomainService.findByCode()
   ↓ 调用 userDomainService.updateUserProfile()
   
3. UserDomainService.updateUserProfile()
   ↓ 调用 user.updateProfile()（实体方法，执行验证）
   ↓ 调用 userRepository.save()
   
4. User.updateProfile() [Entity 内部]
   ↓ 验证昵称长度
   ↓ 验证邮箱格式
   ↓ 验证手机号格式
   ↓ 更新字段
   ↓ 设置 updateTime
   
5. UserAppService.updateCurrentUser()
   ↓ 转换 Entity → DTO
   ↓ 提交事务
   ↓ 返回 UserDTO
```

## 🆚 新旧架构对比

### 旧架构（core 层）

```
core/
├── controller/
│   └── UserProfileController.java   # 直接调用 Service
├── service/
│   ├── UserService.java             # 混合了事务和业务逻辑
│   └── impl/
│       └── UserServiceImpl.java     # 包含 DTO 转换、业务逻辑
├── repository/
│   └── UserRepository.java          # 直接操作 PO
└── entity/
    ├── po/                          # 持久化对象
    └── dto/                         # 数据传输对象
```

**问题**：
- ❌ 职责不清：Service 层既包含事务管理，又包含业务逻辑，还做 DTO 转换
- ❌ 贫血模型：Entity 只是数据容器，没有业务行为
- ❌ 依赖混乱：Domain 层直接依赖 MyBatis-Plus

### 新架构（DDD 四层）

```
interfaces/  → 只负责接收请求和返回响应
application/ → 只负责事务管理和 DTO 转换
domain/      → 只包含业务逻辑和领域模型
infrastructure/ → 只负责数据访问
```

**优势**：
- ✅ 职责清晰：每层只做自己的事情
- ✅ 充血模型：Entity 包含业务行为，领域规则在 Entity 中执行
- ✅ 依赖倒置：Domain 层定义接口，Infrastructure 层实现
- ✅ 易于测试：可以单独测试 Domain 层的业务逻辑
- ✅ 易于扩展：新增业务只需添加 Domain Service 方法

## 📝 使用指南

### 如何新增用户相关接口？

1. **在 Interfaces 层添加 Request DTO**
   ```java
   // interfaces/user/request/YourNewRequest.java
   @Data
   public class YourNewRequest {
       @NotBlank private String field;
   }
   ```

2. **在 Application 层扩展 AppService 接口**
   ```java
   // application/user/service/UserAppService.java
   YourDTO yourNewMethod(String param);
   ```

3. **在 Application 层实现 AppService**
   ```java
   // application/user/service/impl/UserAppServiceImpl.java
   @Override
   @Transactional
   public YourDTO yourNewMethod(String param) {
       // 调用 DomainService
       User user = userDomainService.findByCode(param);
       // DTO 转换
       return toDTO(user);
   }
   ```

4. **在 Interfaces 层添加 Controller 方法**
   ```java
   // interfaces/user/controller/UserController.java
   @PostMapping("/your-new-api")
   public Result<YourDTO> yourNewMethod(@Valid @RequestBody YourNewRequest request) {
       YourDTO result = userAppService.yourNewMethod(request.getField());
       return Result.success(result);
   }
   ```

### 如何添加新的业务逻辑？

1. **在 Domain 层扩展 Entity 方法**
   ```java
   // domain/user/model/entity/User.java
   public void yourNewBusinessMethod(String param) {
       // 业务验证
       if (param == null) {
           throw new DomainException("参数不能为空");
       }
       // 业务逻辑
       this.field = param;
   }
   ```

2. **在 Domain 层扩展 DomainService 接口**
   ```java
   // domain/user/service/UserDomainService.java
   void yourNewDomainMethod(User user, String param);
   ```

3. **在 Application 层实现 DomainService**
   ```java
   // application/user/service/impl/UserDomainServiceImpl.java
   @Override
   public void yourNewDomainMethod(User user, String param) {
       user.yourNewBusinessMethod(param);
       userRepository.save(user);
   }
   ```

## 🚀 启用 OpenFeign（未来扩展）

当前已预留 OpenFeign 接口，启用步骤：

1. **添加依赖**（pom.xml）
   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-openfeign</artifactId>
   </dependency>
   ```

2. **启用 Feign**（启动类）
   ```java
   @SpringBootApplication
   @EnableFeignClients
   public class BootstrapApplication {
       public static void main(String[] args) {
           SpringApplication.run(BootstrapApplication.class, args);
       }
   }
   ```

3. **取消注释**（UserFeignClient.java）
   ```java
   @FeignClient(name = "fish-chat-user", path = "/api/user")
   public interface UserFeignClient {
       @GetMapping("/{code}")
       Result<UserDTO> getUserByCode(@PathVariable("code") String code);
   }
   ```

## ⚠️ 注意事项

1. **事务边界**
   - ✅ 只在 AppService 的写操作上加 `@Transactional`
   - ❌ DomainService 不加事务（由 AppService 管理）

2. **DTO 转换**
   - ✅ AppService 负责 Entity ↔ DTO 转换
   - ❌ DomainService 不接触 DTO

3. **业务逻辑位置**
   - ✅ 简单验证：放在 Entity 中
   - ✅ 跨实体操作：放在 DomainService 中
   - ❌ AppService 不包含业务逻辑

4. **异常处理**
   - ✅ Domain 层抛 `DomainException`
   - ✅ Application 层抛 `AppException`
   - ✅ Interfaces 层不抛异常，由全局异常处理器统一处理

## 📊 文件清单

### 新增文件

| 文件路径 | 说明 |
|---------|------|
| `domain/.../user/service/UserDomainService.java` | 领域服务接口 |
| `application/.../user/service/UserAppService.java` | 应用服务接口（已重构） |
| `application/.../user/service/impl/UserAppServiceImpl.java` | 应用服务实现 |
| `application/.../user/service/impl/UserDomainServiceImpl.java` | 领域服务实现 |
| `interfaces/.../user/controller/UserController.java` | REST API 控制器 |
| `interfaces/.../user/request/UserRegisterRequest.java` | 注册请求 DTO |
| `interfaces/.../user/request/UserLoginRequest.java` | 登录请求 DTO |
| `interfaces/.../user/request/UserUpdateRequest.java` | 更新资料请求 DTO |
| `interfaces/.../user/request/ChangePasswordRequest.java` | 修改密码请求 DTO |
| `interfaces/.../user/feign/UserFeignClient.java` | OpenFeign 接口（预留） |

### 保留文件

| 文件路径 | 说明 |
|---------|------|
| `domain/.../user/model/entity/User.java` | 用户实体（已重构为充血模型） |
| `domain/.../user/repository/UserRepository.java` | 用户仓储接口 |
| `infrastructure/.../persistence/repository/UserRepositoryImpl.java` | 用户仓储实现 |
| `infrastructure/.../persistence/converter/UserConverter.java` | 用户转换器 |
| `infrastructure/.../persistence/mapper/UserMapper.java` | 用户 Mapper |
| `infrastructure/.../persistence/po/UserPO.java` | 用户持久化对象 |
| `application/.../user/dto/UserDTO.java` | 用户数据传输对象 |

### 待废弃文件

| 文件路径 | 说明 |
|---------|------|
| `core/.../controller/UserProfileController.java` | 旧控制器（待删除） |
| `core/.../service/UserService.java` | 旧服务接口（待删除） |
| `core/.../service/impl/UserServiceImpl.java` | 旧服务实现（待删除） |

## 🎉 总结

本次重构实现了：
- ✅ 清晰的 DDD 四层架构
- ✅ 职责分明的分层设计
- ✅ 充血领域模型
- ✅ 事务与业务逻辑分离
- ✅ 预留 OpenFeign 能力
- ✅ 完整的参数校验
- ✅ 统一的响应格式

重构后的代码更易维护、更易测试、更易扩展！
