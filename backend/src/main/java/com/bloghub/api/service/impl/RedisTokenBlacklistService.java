package com.bloghub.api.service.impl;

import com.bloghub.api.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String KEY_PREFIX = "bloghub:auth:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final Map<String, Instant> fallbackBlacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String accessToken, Duration ttl) {
        if (accessToken == null || accessToken.isBlank() || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }

        String key = key(accessToken);
        try {
            redisTemplate.opsForValue().set(key, "revoked", ttl);
        } catch (RuntimeException ex) {
            fallbackBlacklist.put(key, Instant.now().plus(ttl));
            log.warn("Redis unavailable while blacklisting token; using in-memory fallback: {}", ex.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        String key = key(accessToken);
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (RuntimeException ex) {
            Instant expiresAt = fallbackBlacklist.get(key);
            if (expiresAt == null) {
                return false;
            }
            if (expiresAt.isBefore(Instant.now())) {
                fallbackBlacklist.remove(key);
                return false;
            }
            return true;
        }
    }

    private String key(String token) {
        return KEY_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}
