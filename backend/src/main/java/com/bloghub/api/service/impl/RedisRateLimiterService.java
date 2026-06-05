package com.bloghub.api.service.impl;

import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiterService implements RateLimiterService {

    private static final String KEY_PREFIX = "bloghub:ratelimit:";

    private final StringRedisTemplate redisTemplate;
    private final Map<String, LocalCounter> fallbackCounters = new ConcurrentHashMap<>();

    @Override
    public void checkAllowed(String key, int limit, Duration window) {
        if (limit <= 0) {
            return;
        }

        String redisKey = KEY_PREFIX + key;
        long count = increment(redisKey, window);
        if (count > limit) {
            throw new BlogApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Please retry later.");
        }
    }

    private long increment(String key, Duration window) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, window);
            }
            return count == null ? 1L : count;
        } catch (RuntimeException ex) {
            log.warn("Redis unavailable for rate limiting; using in-memory fallback: {}", ex.getMessage());
            return incrementFallback(key, window);
        }
    }

    private long incrementFallback(String key, Duration window) {
        Instant now = Instant.now();
        LocalCounter counter = fallbackCounters.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiresAt().isBefore(now)) {
                return new LocalCounter(new AtomicInteger(1), now.plus(window));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        return counter.count().get();
    }

    private record LocalCounter(AtomicInteger count, Instant expiresAt) {
    }
}
