package com.bloghub.api.service.impl;

import com.bloghub.api.service.CacheService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redisTemplate;
    private final MeterRegistry meterRegistry;

    @Override
    public Optional<String> get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            metric(value == null ? "miss" : "hit").increment();
            return Optional.ofNullable(value);
        } catch (RuntimeException ex) {
            metric("error").increment();
            log.warn("Redis cache get failed for key {}: {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException ex) {
            metric("error").increment();
            log.warn("Redis cache put failed for key {}: {}", key, ex.getMessage());
        }
    }

    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            metric("error").increment();
            log.warn("Redis cache evict failed for key {}: {}", key, ex.getMessage());
        }
    }

    @Override
    public long increment(String key, Duration ttl) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            if (value != null && value == 1L) {
                redisTemplate.expire(key, ttl);
            }
            return value == null ? 0L : value;
        } catch (RuntimeException ex) {
            metric("error").increment();
            log.warn("Redis counter increment failed for key {}: {}", key, ex.getMessage());
            return 0L;
        }
    }

    private Counter metric(String result) {
        return Counter.builder("bloghub.cache.requests")
                .tag("result", result)
                .register(meterRegistry);
    }
}
