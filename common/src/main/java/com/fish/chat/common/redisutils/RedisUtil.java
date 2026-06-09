package com.fish.chat.common.redisutils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 
 * 分布式锁使用 Redisson 实现，支持：
 * 1. WatchDog 自动续期（默认 30 秒）
 * 2. 可重入锁
 * 3. 公平锁、读写锁等
 * 4. 原子性保证
 * 
 * 使用示例：
 * <pre>
 * // 示例 1：带 WatchDog 自动续期
 * RLock lock = redisUtil.lock("myLock", uuid);
 * try {
 *     // 业务逻辑
 * } finally {
 *     redisUtil.release(lock);
 * }
 * 
 * // 示例 2：指定超时时间
 * boolean success = redisUtil.tryLock("myLock", uuid, 5000, 30000);
 * if (success) {
 *     try {
 *         // 业务逻辑
 *     } finally {
 *         redisUtil.release("myLock", uuid);
 *     }
 * }
 * </pre>
 */
@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private RedissonClient redissonClient;


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
     * 尝试获取分布式锁（使用 Redisson 实现）
     *
     * @param key  锁key
     * @param uuid 线程唯一标识（Redisson 不需要，保留用于兼容）
     * @param waitTime 最大等待时间（毫秒）
     * @param leaseTime 锁自动释放时间（毫秒），-1 表示启用 WatchDog 自动续期
     */
    public boolean tryLock(String key, String uuid, long waitTime, long leaseTime) {
        RLock lock = redissonClient.getLock(key);
        try {
            // 尝试获取锁，等待 waitTime，锁过期时间 leaseTime
            // 如果 leaseTime 为 -1，则启用 WatchDog 自动续期（默认 30 秒）
            boolean success = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            if (success) {
                log.info("获取锁成功: {}", key);
            } else {
                log.warn("获取锁超时: {}, 等待时间: {}ms", key, waitTime);
            }
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断: {}", key);
            return false;
        }
    }
    
    /**
     * 获取分布式锁（带 WatchDog 自动续期）
     * 注意：使用此方法时，必须手动调用 release() 释放锁
     *
     * @param lockKey  锁key
     * @param uuid     线程唯一标识（Redisson 不需要，保留用于兼容）
     */
    public RLock lock(String lockKey, String uuid) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(); // 默认启用 WatchDog，30 秒自动续期
        log.info("新加锁: {}, redisKey: {}", true, lockKey);
        return lock;
    }
    
    /**
     * 获取分布式锁（指定过期时间）
     *
     * @param lockKey  锁key
     * @param uuid     线程唯一标识（Redisson 不需要，保留用于兼容）
     * @param timeout  锁过期时间
     * @param timeUnit 时间单位
     */
    public RLock lock(String lockKey, String uuid, long timeout, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, timeUnit);
        log.info("新加锁: true, redisKey: {}", lockKey);
        return lock;
    }

    /**
     * 释放分布式锁（使用 Redisson）
     *
     * @param lockKey 锁key
     * @param uuid    线程唯一标识（Redisson 不需要，保留用于兼容）
     */
    public void release(String lockKey, String uuid) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("解锁成功: {}", lockKey);
            } else {
                log.warn("解锁失败，当前线程未持有锁: {}", lockKey);
            }
        } catch (Exception e) {
            log.error("解锁异常, key: {}, uuid: {}", lockKey, uuid, e);
        }
    }
    
    /**
     * 释放分布式锁（使用 RLock 对象）
     */
    public void release(RLock lock) {
        try {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("解锁成功: {}", lock.getName());
            }
        } catch (Exception e) {
            log.error("解锁异常", e);
        }
    }

    // ==================== Redis Set 操作 ====================

    /**
     * 向 Set 中添加元素
     */
    public void addToSet(String setKey, String value) {
        try {
            redisTemplate.opsForSet().add(setKey, value);
        } catch (Exception e) {
            log.error("向Set添加元素失败: setKey={}, value={}", setKey, value, e);
        }
    }

    /**
     * 从 Set 中移除元素
     */
    public void removeFromSet(String setKey, String value) {
        try {
            redisTemplate.opsForSet().remove(setKey, value);
        } catch (Exception e) {
            log.error("从Set移除元素失败: setKey={}, value={}", setKey, value, e);
        }
    }

    /**
     * 获取 Set 中的所有元素
     */
    public Set<String> getSetMembers(String setKey) {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(setKey);
            if (members == null) {
                return new HashSet<>();
            }
            Set<String> result = new HashSet<>();
            for (Object member : members) {
                if (member != null) {
                    result.add(member.toString());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("获取Set成员失败: setKey={}", setKey, e);
            return new HashSet<>();
        }
    }

    /**
     * 判断元素是否在 Set 中
     */
    public boolean isMemberOfSet(String setKey, String value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(setKey, value));
        } catch (Exception e) {
            log.error("判断Set成员失败: setKey={}, value={}", setKey, value, e);
            return false;
        }
    }
}
