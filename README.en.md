# Fish Chat

Fish Chat is a real-time chat system implemented using Java + Netty WebSocket, designed with a microservices architecture. It supports user registration and login, real-time message sending and receiving, and online status management.

## Technology Stack

- **Backend Framework**: Spring Boot 2.x
- **Database**: MySQL + MongoDB
- **Cache**: Redis
- **Authentication**: Sa-Token
- **WebSocket**: Netty
- **ORM**: MyBatis Plus

## Project Structure

```
fish-chat/
├── bootstrap/          # Boot module
│   └── src/main/java/com/fish/chat/bootstrap/
│       └── config/     # Configuration classes
├── common/             # Common module
│   └── src/main/java/com/fish/chat/common/
│       ├── constants/ # Constant definitions
│       ├── entity/    # Entity base classes
│       ├── enums/     # Enumerations
│       ├── exception/ # Exception handling
│       ├── properties/# Configuration properties
│       ├── redisutils/# Redis utilities
│       ├── repository/# Base repository
│       ├── result/    # Unified response
│       └── utils/     # General utilities
└── core/              # Core business module
    └── src/main/java/com/fish/chat/core/
        ├── controller/# Controllers
        ├── entity/    # Business entities
        ├── mapper/    # MyBatis Mappers
        ├── netty/     # Netty WebSocket
        ├── repository/# Business repositories
        └── service/   # Business services
```

## Main Features

### User Module
- User registration
- User login
- Retrieve/update user profile
- User search

### Message Module
- Real-time message sending and receiving
- Support for message types (e.g., text)
- Message history storage

### Online Status
- User online status management
- List of online users
- Heartbeat detection

## Quick Start

### Prerequisites

- JDK 8+
- Maven 3.x
- MySQL 5.7+
- MongoDB 4.x
- Redis 5.x

### Build and Run

```bash
# Build the project
mvn clean package

# Start the application
java -jar fish-chat-bootstrap/target/fish-chat-bootstrap.jar
```

### Docker Deployment

```bash
docker-compose up -d
```

## Configuration

Configuration files are located in `bootstrap/src/main/resources/`:

- `application.yaml` - Main configuration
- `application-dev.yaml` - Development environment configuration
- `application-prod.yaml` - Production environment configuration

### Core Configuration Items

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

## API Endpoints

### Authentication Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/register` | POST | User registration |
| `/auth/login` | POST | User login |
| `/auth/logout` | POST | User logout |

### User Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/user/profile` | GET | Get current user profile |
| `/user/profile` | POST | Update user profile |
| `/user/{code}` | GET | Get user by code |

### File Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/file/upload` | POST | Upload file |
| `/file/download/{fileName}` | GET | Download file |

### WebSocket

WebSocket Connection URL: `ws://localhost:8081/ws`

Connection Parameters:
- `token`: Authentication token

## License

MIT License