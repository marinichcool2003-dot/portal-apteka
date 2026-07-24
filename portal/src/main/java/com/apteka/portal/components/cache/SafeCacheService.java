package com.apteka.portal.components.cache;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SafeCacheService {

    private final CacheManager cacheManager;

    public <T> Optional<T> get(String cacheName, Object key, Class<T> valueType) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(cache.get(key, valueType));
        } catch (RuntimeException exception) {
            handleGetFailure(cache, key, exception);
            return Optional.empty();
        }
    }

    public <T> Optional<List<T>> getList(String cacheName, Object key, Class<T> elementType) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return Optional.empty();
        }

        try {
            List<?> value = cache.get(key, List.class);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(value.stream().map(elementType::cast).map(v -> (T) v).toList());
        } catch (RuntimeException exception) {
            handleGetFailure(cache, key, exception);
            return Optional.empty();
        }
    }

    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }

        try {
            cache.put(key, value);
        } catch (RuntimeException exception) {
            log.error("Redis cache write failed: cache={}, key={}, exception={}",
                    cacheName, key, exception.getClass().getName(), exception);
        }
    }

    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }

        try {
            cache.evict(key);
        } catch (RuntimeException exception) {
            log.error("Redis cache eviction failed: cache={}, key={}, exception={}",
                    cacheName, key, exception.getClass().getName(), exception);
        }
    }

    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        try {
            cache.clear();
        } catch (RuntimeException exception) {
            log.error("Redis cache clear failed: cache={}, exception={}",
                    cacheName, exception.getClass().getName(), exception);
        }
    }

    private void handleGetFailure(Cache cache, Object key, RuntimeException exception) {
        if (hasCause(exception, RedisConnectionFailureException.class)) {
            log.warn("Redis connection failed during cache read; treating as cache miss: cache={}, key={}",
                    cache.getName(), key, exception);
            return;
        }

        if (isSerializationOrTypeFailure(exception)) {
            log.warn("Corrupted or incompatible cache value; evicting entry: cache={}, key={}",
                    cache.getName(), key, exception);
            evict(cache.getName(), key);
            return;
        }

        log.error("Redis cache read failed; treating as cache miss: cache={}, key={}, exception={}",
                cache.getName(), key, exception.getClass().getName(), exception);
    }

    private boolean isSerializationOrTypeFailure(Throwable exception) {
        return hasCause(exception, SerializationException.class)
                || hasCause(exception, IllegalStateException.class)
                || hasCause(exception, ClassCastException.class);
    }

    private boolean hasCause(Throwable exception, Class<? extends Throwable> expectedType) {
        Throwable current = exception;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
