package com.bloghub.api.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration("popular-posts", cacheConfig(Duration.ofMinutes(5)))
                .withCacheConfiguration("search-results", cacheConfig(Duration.ofMinutes(2)))
                .withCacheConfiguration("post-view-counts", cacheConfig(Duration.ofHours(6)));
    }

    private RedisCacheConfiguration cacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(ttl);
    }
}
