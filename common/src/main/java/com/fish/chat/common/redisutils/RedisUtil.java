package com.fish.chat.common.redisutils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类（含分布式锁）
 */
@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<List> releaseLockScript;

    @PostConstruct
    public void init() {
        String luaStr = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        ByteArrayResource resource = new ByteArrayResource(luaStr.getBytes());

        releaseLockScript = new DefaultRedisScript<>();
        releaseLockScript.setResultType(List.class);
        releaseLockScript.setScriptSource(new ResourceScriptSource(resource));
    }


    /**
     * 存储键值对到redis中，并设置过期时间
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            result = true;
        } catch (Exception e) {
            log.error("设置缓存key:{}失败", key, e);
        }
        return result;
    }

    /**
     * 存储键值对到redis中，并设置过期时间（单位：秒）
     */
    public boolean setValueWithExpire(String key, Object value, long time) {
        boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            result = true;
        } catch (Exception e) {
            log.error("设置缓存key:{}失败", key);
        }
        return result;
    }

    /**
     * 存储键值对到redis中（永久有效）
     */
    public boolean setValueOnly(String key, Object value) {
        boolean result = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            result = true;
        } catch (Exception e) {
            log.error("设置缓存key:{}失败", key);
        }
        return result;
    }

    /**
     * 获取指定key的value值
     */
    public Object getByKey(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定key及其对应的value值
     */
    public void deleteByKey(String key) {
        redisTemplate.delete(key);
    }


    /**
     * 设置指定key的过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        boolean result = false;
        try {
            result = redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存key:{}过期时间失败", key, e);
        }
        return result;
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 扫描匹配指定模式的key集合（使用SCAN避免阻塞Redis）
     */
    public Set<String> scanKeys(String pattern) {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> result = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern.getBytes(StandardCharsets.UTF_8))
                    .count(100)
                    .build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return result;
        });
    }

    /**
     * 尝试获取分布式锁（带超时重试，使用循环避免栈溢出）
     *
     * @param key  锁key
     * @param uuid 线程唯一标识
     * @param maxWaitTime 最大等待时间（毫秒）
     */
    public boolean tryLock(String key, String uuid, long maxWaitTime) {
        long startTime = System.currentTimeMillis();
        long remainingTime = maxWaitTime;
        
        while (remainingTime > 0) {
            // 尝试获取锁，锁过期时间至少为100ms
            if (lock(key, uuid, Math.max(remainingTime, 100), TimeUnit.MILLISECONDS)) {
                return true;
            }
            
            try {
                // 动态计算休眠时间，避免过度等待
                long sleepTime = Math.min(50, remainingTime);
                Thread.sleep(sleepTime);
                
                // 计算剩余等待时间
                remainingTime = maxWaitTime - (System.currentTimeMillis() - startTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁被中断: {}", key);
                return false;
            }
        }
        
        log.warn("获取锁超时: {}, 最大等待时间: {}ms", key, maxWaitTime);
        return false;
    }

    /**
     * 获取分布式锁（可重入）
     *
     * @param lockKey  锁key
     * @param uuid     线程唯一标识
     * @param timeout  锁过期时间
     * @param timeUnit 时间单位
     */
    public boolean lock(String lockKey, String uuid, long timeout, TimeUnit timeUnit) {
        Object currentLock = redisTemplate.opsForValue().get(lockKey);
        if (StringUtils.isEmpty(currentLock)) {
            boolean result = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, timeout, timeUnit));
            log.info("新加锁: {}, redisKey: {}", result, lockKey);
            return result;
        } else {
            if (currentLock.equals(uuid)) {
                redisTemplate.opsForValue().set(lockKey, uuid, timeout, timeUnit);
                log.info("重入锁: true, redisKey: {}", lockKey);
                return true;
            } else {
                log.info("重入锁: false, redisKey: {}", lockKey);
                return false;
            }
        }
    }

    /**
     * 释放分布式锁（Lua脚本保证原子性）
     *
     * @param lockKey 锁key
     * @param uuid    线程唯一标识
     */
    public void release(String lockKey, String uuid) {
        try {
            List<Long> execute = redisTemplate.execute(releaseLockScript, Collections.singletonList(lockKey), uuid);
            boolean result = execute != null && !execute.isEmpty() && execute.get(0).equals(1L);
            log.info("解锁结果: {}, redisKey: {}", result, lockKey);
            if (!result) {
                log.info("解锁失败: {}", lockKey);
            }
        } catch (Exception e) {
            log.error("解锁异常, key: {}, uuid: {}", lockKey, uuid, e);
        }
    }
}
