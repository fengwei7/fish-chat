package com.fish.chat.core.controller;

import com.fish.chat.common.result.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * 提供应用健康状态检查，包括：
 * - 应用基本状态
 * - 数据库连接状态
 * - Redis 连接状态
 * - MongoDB 连接状态
 * 
 * @author fengwei
 * @since 2026-06-09
 */
@Slf4j
@RestController
@RequestMapping("/actuator")
public class HealthController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 健康检查端点
     * 
     * @return 健康状态详情
     */
    @GetMapping("/health")
    public Result<HealthStatus> health() {
        HealthStatus status = new HealthStatus();
        status.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 检查各个组件状态
        Map<String, ComponentStatus> components = new HashMap<>();
        components.put("application", checkApplication());
        components.put("database", checkDatabase());
        components.put("redis", checkRedis());
        components.put("mongodb", checkMongoDB());
        
        status.setComponents(components);
        
        // 判断整体状态：所有组件都 UP 才是 UP
        boolean allUp = components.values().stream()
                .allMatch(c -> "UP".equals(c.getStatus()));
        status.setStatus(allUp ? "UP" : "DOWN");
        
        return Result.success(status);
    }

    /**
     * 检查应用状态
     */
    private ComponentStatus checkApplication() {
        ComponentStatus status = new ComponentStatus();
        status.setStatus("UP");
        status.setDetail("Application is running");
        return status;
    }

    /**
     * 检查数据库连接
     */
    private ComponentStatus checkDatabase() {
        ComponentStatus status = new ComponentStatus();
        try {
            if (jdbcTemplate == null) {
                status.setStatus("UNKNOWN");
                status.setDetail("JdbcTemplate not configured");
                return status;
            }
            
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                status.setStatus("UP");
                status.setDetail("Database connection OK");
            } else {
                status.setStatus("DOWN");
                status.setDetail("Database query failed");
            }
        } catch (Exception e) {
            status.setStatus("DOWN");
            status.setDetail("Database connection failed: " + e.getMessage());
        }
        return status;
    }

    /**
     * 检查 Redis 连接
     */
    private ComponentStatus checkRedis() {
        ComponentStatus status = new ComponentStatus();
        try {
            if (redisTemplate == null) {
                status.setStatus("UNKNOWN");
                status.setDetail("RedisTemplate not configured");
                return status;
            }
            
            // 尝试 ping Redis
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            
            if ("PONG".equalsIgnoreCase(pong)) {
                status.setStatus("UP");
                status.setDetail("Redis connection OK");
            } else {
                status.setStatus("DOWN");
                status.setDetail("Redis ping failed");
            }
        } catch (Exception e) {
            status.setStatus("DOWN");
            status.setDetail("Redis connection failed: " + e.getMessage());
        }
        return status;
    }

    /**
     * 检查 MongoDB 连接
     */
    private ComponentStatus checkMongoDB() {
        ComponentStatus status = new ComponentStatus();
        try {
            if (mongoTemplate == null) {
                status.setStatus("UNKNOWN");
                status.setDetail("MongoTemplate not configured");
                return status;
            }
            
            // 尝试执行简单命令
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            status.setStatus("UP");
            status.setDetail("MongoDB connection OK");
        } catch (Exception e) {
            status.setStatus("DOWN");
            status.setDetail("MongoDB connection failed: " + e.getMessage());
        }
        return status;
    }

    /**
     * 健康状态响应
     */
    @Data
    public static class HealthStatus {
        /** 整体状态：UP/DOWN */
        private String status;
        /** 检查时间 */
        private String timestamp;
        /** 各组件状态 */
        private Map<String, ComponentStatus> components;
    }

    /**
     * 组件状态
     */
    @Data
    public static class ComponentStatus {
        /** 组件状态：UP/DOWN/UNKNOWN */
        private String status;
        /** 详细信息 */
        private String detail;
    }
}
