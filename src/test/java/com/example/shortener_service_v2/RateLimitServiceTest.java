
package com.example.shortener_service_v2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RateLimitService rateLimitService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        rateLimitService = new RateLimitService(redisTemplate);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldIncrementRequests() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(1L);

        Long result = rateLimitService.incrementRequests("127.0.0.1");

        assertEquals(1L, result);
        verify(redisTemplate).expire(
                eq("rate_limit:127.0.0.1"),
                any(java.time.Duration.class)
        );
    }

    @Test
    void shouldNotBeRateLimitedForenRequests() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(10L);

        assertFalse(rateLimitService.isRateLimited("127.0.0.1"));
    }

    @Test
    void shouldBeRateLimitedAfterTenRequests() {
        when(valueOperations.increment("rate_limit:127.0.0.1"))
                .thenReturn(11L);

        assertTrue(rateLimitService.isRateLimited("127.0.0.1"));
    }

    @Test
    void shouldAllowRequestWhenRedisFails() {
        when(redisTemplate.opsForValue())
                .thenThrow(new RuntimeException("Redis unavailable"));

        assertFalse(rateLimitService.isRateLimited("127.0.0.1"));
    }
}
