package com.library.libraryService.security.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiterService {

    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_SECONDS = 60L;

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key) {
        String redisKey = "rate:" + key;

        Long current = redisTemplate.opsForValue().increment(redisKey);

        if (current != null && current == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SECONDS));
        }

        return current != null && current <= MAX_REQUESTS;
    }

    public int getRemaining(String key) {
        String redisKey = "rate:" + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        int current = (value == null) ? 0 : Integer.parseInt(value);
        return Math.max(0, MAX_REQUESTS - current);
    }

    public long getWindowMs() {
        return WINDOW_SECONDS * 1000L;
    }
}
