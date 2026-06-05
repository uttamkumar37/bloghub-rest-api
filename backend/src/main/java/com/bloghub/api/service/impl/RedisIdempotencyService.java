package com.bloghub.api.service.impl;

import com.bloghub.api.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisIdempotencyService implements IdempotencyService {

    private static final String KEY_PREFIX = "bloghub:idempotency:";

    private final StringRedisTemplate redisTemplate;
    private final Map<String, Long> fallbackKeys = new ConcurrentHashMap<>();

    @Override
    public boolean claim(String key, Duration ttl) {
        String namespacedKey = KEY_PREFIX + key;
        try {
            Boolean stored = redisTemplate.opsForValue().setIfAbsent(namespacedKey, "claimed", ttl);
            return Boolean.TRUE.equals(stored);
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable for idempotency; using in-memory fallback: {}", ex.getMessage());
            long expiresAt = System.currentTimeMillis() + ttl.toMillis();
            Long existing = fallbackKeys.putIfAbsent(namespacedKey, expiresAt);
            if (existing == null) {
                return true;
            }
            if (existing < System.currentTimeMillis()) {
                fallbackKeys.put(namespacedKey, expiresAt);
                return true;
            }
            return false;
        }
    }
}
