// package com.apteka.portal.config;

// import org.springframework.cache.Cache;
// import org.springframework.cache.annotation.CachingConfigurer;
// import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.cache.interceptor.CacheErrorHandler;
// import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.cache.RedisCacheConfiguration;
// import org.springframework.data.redis.cache.RedisCacheManager;
// import org.springframework.data.redis.connection.RedisConnectionFactory;

// import lombok.extern.slf4j.Slf4j;

// @Configuration
// @EnableCaching
// @Slf4j
// public class CacheConfig implements CachingConfigurer {

//     @Bean
//     public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
//         RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
//                 .disableCachingNullValues();

//         return RedisCacheManager.builder(connectionFactory)
//                 .cacheDefaults(configuration)
//                 .build();
//     }

//     @Override
//     @Bean
//     public CacheErrorHandler errorHandler() {
//         return new SimpleCacheErrorHandler() {
//             @Override
//             public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
//                 log.error("Redis недоступен при чтении из кэша {}: {}", cache.getName(), exception.getMessage());
//             }

//             @Override
//             public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
//                 log.error("Redis недоступен при записи в кэш {}: {}", cache.getName(), exception.getMessage());
//             }

//             @Override
//             public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
//                 log.error("Redis недоступен при удалении из кэша {}: {}", cache.getName(), exception.getMessage());
//             }

//             @Override
//             public void handleCacheClearError(RuntimeException exception, Cache cache) {
//                 log.error("Redis недоступен при очистке кэша {}: {}", cache.getName(), exception.getMessage());
//             }
//         };
//     }
// }
