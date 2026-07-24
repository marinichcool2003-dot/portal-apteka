package com.apteka.portal.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.dtos.response.mainpagelink.MainPageLinkResponseDTO;
import com.apteka.portal.dtos.response.usergroup.UserGroupResponseDTO;
import com.apteka.portal.models.CacheNames;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            @org.springframework.beans.factory.annotation.Value("${app.cache.key-prefix:portal:v2:}") String cacheKeyPrefix) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith(cacheKeyPrefix);

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(CacheNames.USER_GROUP,
                cacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, UserGroupResponseDTO.class));
        cacheConfigurations.put(CacheNames.USER_GROUPS_LIST,
                listCacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, UserGroupResponseDTO.class));
        cacheConfigurations.put(CacheNames.USER_GROUPS_VISIBLE,
                listCacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, UserGroupResponseDTO.class));
        cacheConfigurations.put(CacheNames.GROUP_TASK,
                cacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, GroupTaskResponseDTO.class));
        cacheConfigurations.put(CacheNames.GROUP_TASKS_BY_GROUP,
                listCacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, GroupTaskResponseDTO.class));
        cacheConfigurations.put(CacheNames.WORK_TYPE,
                cacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, WorkTypeResponseDTO.class));
        cacheConfigurations.put(CacheNames.WORK_TYPES_BY_GROUP,
                listCacheConfiguration(defaultConfiguration, Duration.ofDays(3), objectMapper, WorkTypeResponseDTO.class));
        cacheConfigurations.put(CacheNames.GROUPS_USER_STATS,
                listCacheConfiguration(defaultConfiguration, Duration.ofSeconds(30), objectMapper, DepartmentTaskStatsDTO.class));
        cacheConfigurations.put(CacheNames.GROUP_USER_STATS,
                cacheConfiguration(defaultConfiguration, Duration.ofSeconds(30), objectMapper, DepartmentTaskStatsDTO.class));
        cacheConfigurations.put(CacheNames.MAIN_PAGE_LINKS,
                listCacheConfiguration(defaultConfiguration, Duration.ofDays(30), objectMapper, MainPageLinkResponseDTO.class));
        cacheConfigurations.put(CacheNames.MAIN_PAGE_LINKS_BY_GROUP,
                listCacheConfiguration(defaultConfiguration, Duration.ofDays(30), objectMapper, MainPageLinkResponseDTO.class));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }


    private RedisCacheConfiguration cacheConfiguration(RedisCacheConfiguration defaultConfiguration, Duration ttl,
            ObjectMapper objectMapper, Class<?> dtoType) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(dtoType);
        return defaultConfiguration.entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, javaType)));
    }

    private RedisCacheConfiguration listCacheConfiguration(RedisCacheConfiguration defaultConfiguration, Duration ttl,
            ObjectMapper objectMapper, Class<?> dtoType) {
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, dtoType);
        return defaultConfiguration.entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, javaType)));
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis cache read failed: cache={}, key={}, exception={}",
                        cache.getName(), key, exception.getClass().getName(), exception);
                try {
                    cache.evict(key);
                    log.info("Evicted corrupted cache entry: {}:{}", cache.getName(), key);
                } catch (Exception e) {
                    log.error("Failed to evict cache entry: cache={}, key={}, exception={}",
                            cache.getName(), key, e.getClass().getName(), e);
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Redis cache write failed: cache={}, key={}, exception={}",
                        cache.getName(), key, exception.getClass().getName(), exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis cache eviction failed: cache={}, key={}, exception={}",
                        cache.getName(), key, exception.getClass().getName(), exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Redis cache clear failed: cache={}, exception={}",
                        cache.getName(), exception.getClass().getName(), exception);
            }
        };
    }
}
