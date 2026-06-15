package com.apteka.portal.config;

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

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import com.apteka.portal.models.CacheNames;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.")
                .allowIfSubType("com.apteka.portal.dtos.")
                .build();

        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .activateDefaultTyping(
                        ptv,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        RedisCacheConfiguration userGroupConfig = defaultConfiguration.entryTtl(Duration.ofDays(3));
        cacheConfigurations.put(CacheNames.USER_GROUPS_LIST, userGroupConfig);
        cacheConfigurations.put(CacheNames.USER_GROUP, userGroupConfig);

        RedisCacheConfiguration groupTaskConfig = defaultConfiguration.entryTtl(Duration.ofDays(3));
        cacheConfigurations.put(CacheNames.GROUP_TASK, groupTaskConfig);
        cacheConfigurations.put(CacheNames.GROUP_TASKS_BY_GROUP, groupTaskConfig);

        RedisCacheConfiguration workTypeConfig = defaultConfiguration.entryTtl(Duration.ofDays(3));
        cacheConfigurations.put(CacheNames.WORK_TYPE, workTypeConfig);
        cacheConfigurations.put(CacheNames.WORK_TYPES_BY_GROUP, workTypeConfig);

        RedisCacheConfiguration groupsStatsConfig = defaultConfiguration.entryTtl(Duration.ofSeconds(30));
        cacheConfigurations.put(CacheNames.GROUPS_USER_STATS, groupsStatsConfig);

        RedisCacheConfiguration userStatsConfig = defaultConfiguration.entryTtl(Duration.ofSeconds(10));
        cacheConfigurations.put(CacheNames.USER_STATS, userStatsConfig);

        RedisCacheConfiguration groupsMainPageLinks = defaultConfiguration.entryTtl(Duration.ofDays(30));
        cacheConfigurations.put(CacheNames.GROUPS_MAIN_PAGE_LINKS, groupsMainPageLinks);

        RedisCacheConfiguration mainPageLinks= defaultConfiguration.entryTtl(Duration.ofDays(30));
        cacheConfigurations.put(CacheNames.MAIN_PAGE_LINKS_BY_GROUP, mainPageLinks);
        cacheConfigurations.put(CacheNames.MAIN_PAGE_LINKS, mainPageLinks);
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {

            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Ошибка чтения Redis cache {}: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Ошибка записи Redis cache {}: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Ошибка удаления Redis cache {}: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Ошибка очистки Redis cache {}: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}