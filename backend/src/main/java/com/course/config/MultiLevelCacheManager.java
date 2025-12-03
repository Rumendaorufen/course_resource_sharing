package com.course.config;

import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多级缓存管理器
 * 管理一级缓存（Caffeine）和二级缓存（Redis）
 */
public class MultiLevelCacheManager extends CaffeineCacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;

    public MultiLevelCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Nullable
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, cacheName -> {
            // 创建Caffeine缓存
            CaffeineCache caffeineCache = (CaffeineCache) super.getCache(cacheName);
            if (caffeineCache == null) {
                // 如果父类没有创建成功，创建一个新的Caffeine缓存
                caffeineCache = new CaffeineCache(cacheName, super.createNativeCaffeineCache(cacheName));
            }
            // 包装为多级缓存
            return new MultiLevelCache(cacheName, caffeineCache, redisTemplate);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }

    // 初始化缓存
    public void initCaches() {
        // 初始化配置的缓存
        Collection<String> cacheNames = super.getCacheNames();
        if (cacheNames != null) {
            for (String cacheName : cacheNames) {
                getCache(cacheName);
            }
        }
    }
}