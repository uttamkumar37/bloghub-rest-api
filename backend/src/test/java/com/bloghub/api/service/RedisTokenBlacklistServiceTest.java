package com.bloghub.api.service;

import com.bloghub.api.service.impl.RedisTokenBlacklistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DisplayName("RedisTokenBlacklistService unit tests")
class RedisTokenBlacklistServiceTest {

    @Test
    @DisplayName("blacklist - fallback remembers token when Redis is unavailable")
    void blacklist_fallback() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        given(redisTemplate.opsForValue()).willThrow(new RedisConnectionFailureException("redis down"));
        given(redisTemplate.hasKey(org.mockito.ArgumentMatchers.anyString()))
                .willThrow(new RedisConnectionFailureException("redis down"));
        RedisTokenBlacklistService service = new RedisTokenBlacklistService(redisTemplate);

        service.blacklist("access-token", Duration.ofMinutes(5));

        assertThat(service.isBlacklisted("access-token")).isTrue();
    }
}
