package com.bloghub.api.service;

import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.service.impl.RedisRateLimiterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("RedisRateLimiterService unit tests")
class RedisRateLimiterServiceTest {

    @Test
    @DisplayName("checkAllowed - fallback limiter blocks after limit")
    void checkAllowed_fallbackBlocksAfterLimit() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        given(redisTemplate.opsForValue()).willThrow(new RedisConnectionFailureException("redis down"));
        RedisRateLimiterService service = new RedisRateLimiterService(redisTemplate);

        service.checkAllowed("auth:login:127.0.0.1", 1, Duration.ofMinutes(1));

        assertThatThrownBy(() -> service.checkAllowed("auth:login:127.0.0.1", 1, Duration.ofMinutes(1)))
                .isInstanceOf(BlogApiException.class)
                .hasMessageContaining("Rate limit exceeded");
    }
}
