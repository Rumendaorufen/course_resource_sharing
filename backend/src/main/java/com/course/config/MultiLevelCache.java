package com.course.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存实现类
 * 一级缓存：Caffeine
 * 二级缓存：Redis
 */
@Slf4j
public class MultiLevelCache implements Cache {

    private final String name;
    private final CaffeineCache caffeineCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long REDIS_EXPIRE_TIME = 60 * 60; // 60分钟

    public MultiLevelCache(String name, CaffeineCache caffeineCache, RedisTemplate<String, Object> redisTemplate) {
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return caffeineCache.getNativeCache();
    }

    @Override
    @Nullable
    public <T> T get(Object key, Class<T> type) {
        // 1. 查询一级缓存（Caffeine）
        T value = caffeineCache.get(key, type);
        if (value != null) {
            log.debug("一级缓存命中，缓存名称：{}, 键：{}", name, key);
            return value;
        }

        // 2. 查询二级缓存（Redis）
        try {
            String redisKey = generateRedisKey(key);
            Object redisValue = redisTemplate.opsForValue().get(redisKey);
            if (redisValue != null) {
                log.debug("二级缓存命中，缓存名称：{}, 键：{}", name, key);
                // 回填到一级缓存
                caffeineCache.put(key, redisValue);
                return type.cast(redisValue);
            }
        } catch (Exception e) {
            // Redis查询失败，记录日志，继续执行
            log.error("Redis查询失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
        }

        log.debug("缓存未命中，缓存名称：{}, 键：{}", name, key);
        return null;
    }

    @Override
    @Nullable
    public <T> T get(Object key, Callable<T> valueLoader) {
        // 1. 查询一级缓存
        ValueWrapper valueWrapper = caffeineCache.get(key);
        if (valueWrapper != null) {
            log.debug("一级缓存命中，缓存名称：{}, 键：{}", name, key);
            return (T) valueWrapper.get();
        }

        // 2. 查询二级缓存
        try {
            String redisKey = generateRedisKey(key);
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value != null) {
                log.debug("二级缓存命中，缓存名称：{}, 键：{}", name, key);
                // 回填到一级缓存
                caffeineCache.put(key, value);
                return (T) value;
            }
        } catch (Exception e) {
            // Redis查询失败，记录日志，继续执行
            log.error("Redis查询失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
        }

        // 3. 查询数据源
        try {
            Object value = valueLoader.call();
            if (value != null) {
                // 同时写入一级和二级缓存
                caffeineCache.put(key, value);
                try {
                    String redisKey = generateRedisKey(key);
                    redisTemplate.opsForValue().set(redisKey, value, REDIS_EXPIRE_TIME, TimeUnit.SECONDS);
                    log.debug("缓存写入成功，缓存名称：{}, 键：{}", name, key);
                } catch (Exception e) {
                    // Redis写入失败，仅记录日志，不影响主流程
                    log.error("Redis写入失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
                }
            }
            return (T) value;
        } catch (Exception e) {
            log.error("从数据源获取数据失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    @Nullable
    public ValueWrapper get(Object key) {
        // 1. 查询一级缓存
        ValueWrapper valueWrapper = caffeineCache.get(key);
        if (valueWrapper != null) {
            log.debug("一级缓存命中，缓存名称：{}, 键：{}", name, key);
            return valueWrapper;
        }

        // 2. 查询二级缓存
        try {
            String redisKey = generateRedisKey(key);
            Object value = redisTemplate.opsForValue().get(redisKey);
            if (value != null) {
                log.debug("二级缓存命中，缓存名称：{}, 键：{}", name, key);
                // 回填到一级缓存
                caffeineCache.put(key, value);
                return () -> value;
            }
        } catch (Exception e) {
            // Redis查询失败，记录日志，继续执行
            log.error("Redis查询失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
        }

        log.debug("缓存未命中，缓存名称：{}, 键：{}", name, key);
        return null;
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        if (value == null) {
            evict(key);
            return;
        }

        // 同时写入一级和二级缓存
        caffeineCache.put(key, value);
        try {
            String redisKey = generateRedisKey(key);
            redisTemplate.opsForValue().set(redisKey, value, REDIS_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("缓存更新成功，缓存名称：{}, 键：{}", name, key);
        } catch (Exception e) {
            // Redis写入失败，仅记录日志，不影响主流程
            log.error("Redis写入失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
        }
    }

    @Override
    @Nullable
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
        if (value == null) {
            return get(key);
        }

        // 先查询缓存
        ValueWrapper existingValue = get(key);
        if (existingValue != null) {
            return existingValue;
        }

        // 写入缓存
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        // 同时删除一级和二级缓存
        caffeineCache.evict(key);
        try {
            String redisKey = generateRedisKey(key);
            redisTemplate.delete(redisKey);
            log.debug("缓存删除成功，缓存名称：{}, 键：{}", name, key);
        } catch (Exception e) {
            // Redis删除失败，仅记录日志，不影响主流程
            log.error("Redis删除失败，缓存名称：{}, 键：{}, 错误信息：{}", name, key, e.getMessage());
        }
    }

    @Override
    public void clear() {
        // 清空一级缓存
        caffeineCache.clear();
        // 清空二级缓存
        try {
            String pattern = name + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.debug("缓存清空成功，缓存名称：{}", name);
        } catch (Exception e) {
            // Redis清空失败，仅记录日志，不影响主流程
            log.error("Redis清空失败，缓存名称：{}, 错误信息：{}", name, e.getMessage());
        }
    }

    /**
     * 生成Redis键
     */
    private String generateRedisKey(Object key) {
        return name + ":" + key.toString();
    }
}