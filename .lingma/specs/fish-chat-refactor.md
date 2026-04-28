# Fish-Chat 即时聊天后端重构计划

## Context

当前项目是一个基于 Spring Boot 的即时聊天后端，存在以下主要问题需要重构：

### 核心问题
1. **严重安全漏洞**：数据库密码硬编码在代码和配置文件中（CodeGenerator.java、application.yaml）
2. **架构反模式**：大量使用静态变量存储依赖（ChatWebSocketHandler、WebSocketStorageUtil）
3. **职责混乱**：WebSocket Handler 承担过多职责（连接管理、消息路由、持久化、心跳处理）
4. **性能隐患**：Redis KEYS 命令阻塞、N+1 查询问题
5. **测试覆盖不足**：仅有一个空的基础测试
6. **Docker 配置错误**：entrypoint.sh 路径不匹配

## 重构目标

创建一个**完备可用、架构清晰、安全可靠**的即时聊天后端项目。

---

## 重构方案

### 一、技术栈调整（基于用户选择）

#### 1.1 保持 Java 1.8 兼容性
由于选择保持 **Java 1.8**，技术栈调整如下：
- **Spring Boot**: 2.6.13 → **2.7.18** (Java 8 兼容的最新稳定版)
- **Java**: **保持 1.8**
- **MyBatis-Plus**: 3.5.13 → **3.5.5** (保持兼容)
- **Sa-Token**: 1.44.0 → **保持 1.44.0** (已兼容)

#### 1.2 依赖优化
- **移除**: Fastjson（安全问题）→ **Jackson**（Spring Boot 默认）
- **优化**: Hutool → 仅保留必要的密码加密功能，其他用 Spring 工具类替代
- **保留并优化**: MySQL、MongoDB、Redis、Druid、WebSocket

#### 1.3 新增依赖
- **Spring Validation**: JSR-303 参数校验（Hibernate Validator）
- **SpringDoc OpenAPI 1.x**: REST API 文档（兼容 Spring Boot 2）
- **Testcontainers**: 集成测试容器
- **MapStruct**: DTO 与 Entity 转换

---

### 二、项目结构重构

#### 2.1 简化三模块结构（基于用户选择）

```
fish-chat/
├── fish-chat-common/          # 公共模块
│   ├── result/                # 统一返回结果（泛型化）
│   ├── exception/             # 全局异常处理
│   ├── constants/             # 常量定义
│   └── utils/                 # 工具类（无状态，纯静态方法）
│
├── fish-chat-core/            # 核心业务模块
│   ├── entity/                # 实体类（User, ChatMessage 等）
│   ├── dto/                   # 数据传输对象
│   ├── mapper/                # MyBatis Mapper
│   ├── repository/            # 仓储层（MongoDB, Redis）
│   ├── service/               # 业务服务层
│   │   ├── AuthService.java   # 认证服务
│   │   ├── UserService.java   # 用户服务
│   │   ├── ChatMessageService.java  # 消息服务
│   │   └── WebSocketService.java    # WebSocket 服务
│   ├── handler/               # WebSocket 消息处理器
│   │   ├── MessageHandler.java      # 处理器接口
│   │   ├── ChatMessageHandler.java  # 聊天消息处理器
│   │   ├── PingMessageHandler.java  # 心跳处理器
│   │   └── MessageDispatcher.java   # 消息分发器
│   └── config/                # 配置类
│       ├── CorsConfig.java           # 跨域配置
│       ├── SaTokenConfigure.java     # 权限配置
│       ├── WebSocketConfig.java      # WebSocket 配置
│       └── CacheConfig.java          # 缓存配置
│
└── fish-chat-bootstrap/       # 启动模块
    ├── BootstrapApplication.java
    └── controller/            # REST Controller
        ├── AuthController.java
        ├── UserController.java
        ├── ChatMessageController.java
        └── WebSocketEndpoint.java  # WebSocket 端点控制器
```

#### 2.2 三层架构
- **Bootstrap**: 启动和适配层（Controller、配置）
- **Core**: 核心业务层（Service、Repository、Handler）
- **Common**: 公共组件层（工具类、异常、Result）

---

### 三、核心代码重构

#### 3.1 消除静态依赖注入反模式

**问题代码**:
```java
// ❌ 错误示例
private static UserService userService;
@Autowired
public void setUserService(UserService userService) {
    ChatWebSocketHandler.userService = userService;
}
```

**重构方案**:
```java
// ✅ 正确做法：实例变量 + 构造函数注入
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final UserService userService;
    private final MessageDispatcher messageDispatcher;
    
    @Autowired
    public ChatWebSocketHandler(UserService userService, MessageDispatcher messageDispatcher) {
        this.userService = userService;
        this.messageDispatcher = messageDispatcher;
    }
}
```

#### 3.2 WebSocket 模块重构

**职责拆分**:

| 原类 | 拆分为 | 职责 |
|------|--------|------|
| `ChatWebSocketHandler` | `WebSocketConnectionManager` | 连接生命周期管理 |
| | `MessageDispatcher` | 消息路由分发 |
| | `UserSessionRegistry` | 会话注册表 |
| `WebSocketStorageUtil` | `UserOnlineRepository` | 在线状态仓储 |
| `WebSocketMessageUtil` | `MessageBuilderFactory` | 消息工厂 |

**消息处理策略模式**:
```java
// 消息处理器接口
public interface MessageHandler {
    String getType();
    void handle(WebSocketSession session, Map<String, Object> payload);
}

// 具体实现
@Component
public class ChatMessageHandler implements MessageHandler {
    @Override
    public String getType() { return "chat"; }
    
    @Override
    public void handle(WebSocketSession session, Map<String, Object> payload) {
        // 处理聊天消息
    }
}

// 分发器
@Component
public class MessageDispatcher {
    private final Map<String, MessageHandler> handlers;
    
    public void dispatch(WebSocketSession session, String type, Map<String, Object> payload) {
        MessageHandler handler = handlers.get(type);
        if (handler != null) {
            handler.handle(session, payload);
        }
    }
}
```

#### 3.3 认证模块重构

**问题**: Controller 中包含业务逻辑（密码加密、盐值生成）

**重构方案**:
```java
// Application Service
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    
    public AuthResponse register(RegisterRequest request) {
        // 业务逻辑封装在此
    }
    
    public AuthResponse login(LoginRequest request) {
        // 业务逻辑封装在此
    }
}

// Controller 只负责 HTTP 协议处理
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authService;
    
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }
}
```

#### 3.4 Redis 性能优化

**问题**: 使用 `keys` 命令导致阻塞

**重构方案**:
```java
// ❌ 错误示例
public Set<String> getOnlineUsers() {
    return redisTemplate.keys("userPO:online:*");
}

// ✅ 正确做法：使用 scan
public Set<String> getOnlineUsers() {
    Set<String> keys = new HashSet<>();
    redisTemplate.execute((RedisCallback<Void>) connection -> {
        try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                .match("userPO:online:*").count(100).build())) {
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
        }
        return null;
    });
    return keys;
}
```

#### 3.5 缓存优化（避免 N+1 查询）

**问题**: 每次 WebSocket 连接都查询 MySQL

**重构方案**:
```java
// 使用 Spring Cache 抽象
@Service
public class UserServiceImpl implements UserService {
    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @CacheEvict(value = "users", key = "#userPO.id")
    public void updateUser(User userPO) {
        userMapper.updateById(userPO);
    }
}

// WebSocket 连接时先查缓存
public void afterConnectionEstablished(WebSocketSession session) {
    Long userId = (Long) session.getAttributes().get("userId");
    User userPO = userService.getUserById(userId); // 自动缓存
    // ...
}
```

#### 3.6 统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
```

#### 3.7 参数校验

```java
// DTO 添加校验注解
public record RegisterRequest(
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 之间")
    String username,
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在 6-32 之间")
    String password,
    
    @Email(message = "邮箱格式不正确")
    String email
) {}

// Controller 启用校验
@PostMapping("/register")
public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return Result.success(authService.register(request));
}
```

---

### 四、安全加固

#### 4.1 敏感配置外部化（基于用户选择）

**移除硬编码密码**，使用环境变量：

```yaml
# application.yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/fish_chat}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}  # 从环境变量读取
  
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/fishchat}
    
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

**Docker Compose 环境变量配置**:
```yaml
# docker-compose.yml
services:
  app:
    environment:
      - DB_URL=jdbc:mysql://mysql:3306/fish_chat
      - DB_USERNAME=root
      - DB_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MONGODB_URI=mongodb://mongo:27017/fishchat
      - REDIS_HOST=redis
```

#### 4.2 CORS 配置修复

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("https://*.example.com") // 限制来源
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

#### 4.3 WebSocket 安全增强

```java
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {
    private final TokenValidator tokenValidator;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 Header 获取 Token（而非 URL 参数，避免日志泄露）
        String token = extractToken(request);
        if (!tokenValidator.validate(token)) {
            return false;
        }
        // 将用户信息存入 attributes
        attributes.put("userId", tokenValidator.getUserId(token));
        return true;
    }
}
```

---

### 五、测试体系完善

#### 5.1 单元测试（JUnit 5 + Mockito）

```java
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthenticationService authService;
    
    @Test
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "password123", "test@example.com");
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        
        // When
        AuthResponse response = authService.register(request);
        
        // Then
        assertNotNull(response.getToken());
        verify(userRepository).save(any(User.class));
    }
}
```

#### 5.2 集成测试（Testcontainers）

```java
@SpringBootTest
@Testcontainers
class ChatMessageIntegrationTest {
    
    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");
    
    @Autowired
    private ChatMessageService messageService;
    
    @Test
    void testSaveAndFindMessages() {
        // Given
        ChatMessage message = new ChatMessage("1", "2", "Hello");
        
        // When
        messageService.save(message);
        List<ChatMessage> messages = messageService.findConversation("1", "2");
        
        // Then
        assertEquals(1, messages.size());
    }
}
```

#### 5.3 测试覆盖目标
- **核心业务逻辑**: 80%+ 覆盖率
- **认证授权模块**: 100% 覆盖
- **WebSocket 消息处理**: 90%+ 覆盖
- **Controller 层**: 重点测试 API 接口

---

### 六、部署配置修复

#### 6.1 Dockerfile 修正（兼容 Java 8）

```dockerfile
FROM openjdk:8-jre-slim
WORKDIR /app
COPY fish-chat-bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 6.2 docker-compose.yml（新增）

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://mysql:3306/fish_chat
      - DB_USERNAME=root
      - DB_PASSWORD=SecurePassword123
      - MONGODB_URI=mongodb://mongo:27017/fishchat
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - mongo
      - redis
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: SecurePassword123
      MYSQL_DATABASE: fish_chat
    volumes:
      - mysql_data:/var/lib/mysql
  
  mongo:
    image: mongo:7
    volumes:
      - mongo_data:/data/db
  
  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  mysql_data:
  mongo_data:
  redis_data:
```

---

### 七、验证 HTML 页面

创建一个简单的 WebSocket 测试页面：

```html
<!DOCTYPE html>
<html>
<head>
    <title>Fish-Chat 验证页面</title>
    <style>
        /* 简洁现代的样式 */
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
        .chat-box { border: 1px solid #ddd; height: 400px; overflow-y: auto; padding: 10px; margin-bottom: 10px; }
        .message { margin: 5px 0; padding: 8px; border-radius: 4px; }
        .sent { background-color: #e3f2fd; text-align: right; }
        .received { background-color: #f5f5f5; }
        input[type="text"] { width: 70%; padding: 8px; }
        button { padding: 8px 16px; background-color: #1976d2; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <h1>Fish-Chat 聊天验证</h1>
    
    <!-- 登录区域 -->
    <div id="loginSection">
        <input type="text" id="username" placeholder="用户名"/>
        <input type="password" id="password" placeholder="密码"/>
        <button onclick="login()">登录</button>
        <button onclick="register()">注册</button>
    </div>
    
    <!-- 聊天区域 -->
    <div id="chatSection" style="display:none;">
        <div class="chat-box" id="messages"></div>
        <input type="text" id="toUserId" placeholder="接收者 ID"/>
        <input type="text" id="messageContent" placeholder="消息内容"/>
        <button onclick="sendMessage()">发送</button>
        <button onclick="disconnect()">断开</button>
    </div>
    
    <script>
        let ws = null;
        let token = null;
        
        async function login() {
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username, password})
            });
            
            const result = await response.json();
            if (result.code === 200) {
                token = result.data.token;
                connectWebSocket(token);
                document.getElementById('loginSection').style.display = 'none';
                document.getElementById('chatSection').style.display = 'block';
            } else {
                alert(result.message);
            }
        }
        
        function connectWebSocket(token) {
            ws = new WebSocket(`ws://localhost:8080/ws/chat?token=${token}`);
            
            ws.onopen = () => {
                appendMessage('system', '连接成功');
            };
            
            ws.onmessage = (event) => {
                const msg = JSON.parse(event.data);
                appendMessage(msg.type, msg.content || msg.message);
            };
        }
        
        function sendMessage() {
            const toUserId = document.getElementById('toUserId').value;
            const content = document.getElementById('messageContent').value;
            
            ws.send(JSON.stringify({
                type: 'chat',
                to: toUserId,
                content: content
            }));
            
            appendMessage('sent', content);
            document.getElementById('messageContent').value = '';
        }
        
        function appendMessage(type, content) {
            const div = document.createElement('div');
            div.className = `message ${type}`;
            div.textContent = content;
            document.getElementById('messages').appendChild(div);
        }
    </script>
</body>
</html>
```

---

## 关键文件清单

### 需要修改的核心文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `pom.xml` | 修改 | 添加子模块引用，升级 Spring Boot 到 2.7.18 |
| `bootstrap/pom.xml` | 重构 | 拆分为 `fish-chat-common`、`fish-chat-core`、`fish-chat-bootstrap` |
| `application.yaml` | 重写 | 外部化敏感配置到环境变量 |
| `ChatWebSocketHandler.java` | 重构 | 改为实例变量 + 构造函数注入，拆分职责 |
| `LoginController.java` | 重构 | 业务逻辑移至 `AuthService` |
| `WebSocketStorageUtil.java` | 删除 | 替换为 `UserOnlineRepository` |
| `WebSocketMessageUtil.java` | 重构 | 改为 `MessageBuilderFactory` 工厂模式 |
| `RedisOnlineUserMapper.java` | 重构 | 使用 SCAN 替代 KEYS 命令 |
| `CodeGenerator.java` | 删除 | 移除硬编码密码的代码生成器 |
| `CorsConfig.java` | 重写 | 修复 CORS 安全配置 |

### 需要新增的文件

#### Common 模块
| 文件 | 说明 |
|------|------|
| `common/result/Result.java` | 统一返回结果（泛型化） |
| `common/exception/BusinessException.java` | 业务异常基类 |
| `common/exception/GlobalExceptionHandler.java` | 全局异常处理 |
| `common/constants/AuthConstants.java` | 认证相关常量 |

#### Core 模块
| 文件 | 说明 |
|------|------|
| `core/service/AuthService.java` | 认证服务（注册、登录、登出） |
| `core/service/UserService.java` | 用户服务（查询、更新） |
| `core/service/ChatMessageService.java` | 消息服务（保存、查询） |
| `core/service/WebSocketService.java` | WebSocket 服务（发送、广播） |
| `core/repository/UserOnlineRepository.java` | Redis 在线用户仓储 |
| `core/repository/MongoChatMessageRepository.java` | MongoDB 消息仓储 |
| `core/handler/MessageHandler.java` | 消息处理器接口 |
| `core/handler/ChatMessageHandler.java` | 聊天消息处理器 |
| `core/handler/PingMessageHandler.java` | 心跳消息处理器 |
| `core/handler/MessageDispatcher.java` | 消息分发器 |
| `core/config/CacheConfig.java` | Spring Cache 配置 |
| `core/config/WebSocketConfig.java` | WebSocket 完整配置 |

#### Bootstrap 模块
| 文件 | 说明 |
|------|------|
| `bootstrap/controller/AuthController.java` | 认证 REST API |
| `bootstrap/controller/UserController.java` | 用户 REST API |
| `bootstrap/controller/ChatMessageController.java` | 消息 REST API |
| `bootstrap/controller/WebSocketEndpoint.java` | WebSocket 端点控制器 |
| `bootstrap/interceptor/SaTokenInterceptor.java` | Sa-Token 拦截器 |

#### 测试文件
| 文件 | 说明 |
|------|------|
| `core/service/AuthServiceTest.java` | 认证服务单元测试 |
| `core/service/UserServiceTest.java` | 用户服务单元测试 |
| `core/handler/ChatMessageHandlerTest.java` | 消息处理器测试 |
| `integration/ChatMessageIntegrationTest.java` | 消息集成测试（Testcontainers） |
| `integration/AuthIntegrationTest.java` | 认证集成测试 |

#### 配置文件
| 文件 | 说明 |
|------|------|
| `docker-compose.yml` | Docker 服务编排 |
| `.env.example` | 环境变量模板 |
| `src/main/resources/static/chat-test.html` | WebSocket 验证页面 |

---

## 验证步骤

### 1. 编译验证
```bash
mvn clean compile
```

### 2. 单元测试（完整测试套件）
```bash
mvn test
# 查看测试覆盖率报告
mvn jacoco:report
```

### 3. 打包验证
```bash
mvn clean package -DskipTests
```

### 4. Docker 构建与运行
```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app
```

### 5. 功能验证

#### 5.1 API 测试
```bash
# 测试注册接口
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

#### 5.2 WebSocket 测试
1. 打开浏览器访问：`http://localhost:8080/chat-test.html`
2. 使用两个不同用户登录（可使用浏览器的无痕模式）
3. 测试点对点聊天功能
4. 查看消息是否正确发送和接收

#### 5.3 API 文档验证
访问 Swagger UI：`http://localhost:8080/swagger-ui.html`

### 6. 性能验证
```bash
# 使用 Apache Bench 进行压力测试
ab -n 1000 -c 10 http://localhost:8080/api/userPO/info
```

### 7. 集成测试
```bash
# 运行集成测试（需要 Docker）
mvn verify -Pintegration-tests
```

---

## 预期成果

✅ **架构清晰**: 三模块结构（common、core、bootstrap），职责单一  
✅ **代码规范**: 无静态依赖注入，符合设计模式（策略模式、工厂模式）  
✅ **安全可靠**: 无硬编码密码（使用环境变量），CORS 配置合理  
✅ **性能优化**: Redis 使用 SCAN 替代 KEYS，Spring Cache 缓存用户数据  
✅ **测试完备**: 单元测试 + 集成测试覆盖核心逻辑（80%+ 覆盖率）  
✅ **部署便捷**: Docker Compose 一键启动所有服务  
✅ **文档齐全**: Swagger API 文档 + WebSocket 验证页面  

---

## 实施步骤

### 阶段一：项目结构调整（1-2 小时）
1. 创建三模块 Maven 结构
   - `fish-chat-common`
   - `fish-chat-core`
   - `fish-chat-bootstrap`
2. 配置父 pom.xml 的依赖管理
3. 迁移现有代码到新模块

### 阶段二：核心代码重构（3-4 小时）
1. 重构 `ChatWebSocketHandler`
   - 消除静态变量
   - 拆分为 `WebSocketConnectionManager`、`MessageDispatcher`、`UserSessionRegistry`
2. 重构 `LoginController`
   - 创建 `AuthService` 处理业务逻辑
   - Controller 只负责 HTTP 协议
3. 重构 `RedisOnlineUserMapper`
   - 使用 SCAN 替代 KEYS
4. 删除工具类反模式
   - `WebSocketStorageUtil` → `UserOnlineRepository`
   - `WebSocketMessageUtil` → `MessageBuilderFactory`

### 阶段三：安全加固（30 分钟）
1. 外部化数据库配置到环境变量
2. 修复 CORS 配置
3. 增强 WebSocket Token 验证（从 Header 获取）
4. 删除 CodeGenerator.java 中的硬编码密码

### 阶段四：测试编写（2-3 小时）
1. 编写单元测试
   - `AuthServiceTest`
   - `UserServiceTest`
   - `ChatMessageHandlerTest`
2. 编写集成测试
   - `AuthIntegrationTest`
   - `ChatMessageIntegrationTest`
3. 运行测试并确保覆盖率达标

### 阶段五：配置与部署（1 小时）
1. 创建 docker-compose.yml
2. 修正 Dockerfile
3. 创建 .env.example 模板
4. 测试 Docker 部署流程

### 阶段六：验证页面（30 分钟）
1. 创建 chat-test.html
2. 实现登录、注册、聊天功能
3. 端到端测试

### 总计预计时间：8-12 小时
