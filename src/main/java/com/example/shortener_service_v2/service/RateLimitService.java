
package com.example.shortener_service_v2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final Logger log =
            LoggerFactory.getLogger(RateLimitService.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long incrementRequests(String ip) {
        try {
            String key = "rate_limit:" + ip;

            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(60));
            }

            return count;
        } catch (RuntimeException exception) {
            log.error("Redis unavailable while incrementing requests for IP", exception);
            return null;
        }
    }

    public boolean isRateLimited(String ip) {
        Long count = incrementRequests(ip);

        return count != null && count > 10;
    }
}
