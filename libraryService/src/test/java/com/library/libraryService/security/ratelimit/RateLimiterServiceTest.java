package com.library.libraryService.security.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    private static final String KEY = "rate:USER_test";

    @Test
    @DisplayName("isAllowed increment pertama (1) → harus set expire & return true")
    void isAllowed_firstIncrement() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(KEY)).thenReturn(1L);

        boolean allowed = rateLimiterService.isAllowed("USER_test");

        assertTrue(allowed);
        verify(redisTemplate).expire(eq(KEY), eq(Duration.ofSeconds(60)));
    }

    @Test
    @DisplayName("isAllowed increment <= 60 → allowed")
    void isAllowed_withinLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(KEY)).thenReturn(30L);

        boolean allowed = rateLimiterService.isAllowed("USER_test");

        assertTrue(allowed);
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    @DisplayName("isAllowed increment > 60 → not allowed")
    void isAllowed_exceedLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(KEY)).thenReturn(70L);

        boolean allowed = rateLimiterService.isAllowed("USER_test");

        assertFalse(allowed);
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    @DisplayName("getRemaining value null → remaining 60")
    void getRemaining_null() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn(null);

        int remaining = rateLimiterService.getRemaining("USER_test");

        assertEquals(60, remaining);
    }

    @Test
    @DisplayName("getRemaining value 20 → remaining 40")
    void getRemaining_value() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn("20");

        int remaining = rateLimiterService.getRemaining("USER_test");

        assertEquals(40, remaining);
    }

    @Test
    @DisplayName("getRemaining value > MAX_REQUESTS → remaining 0")
    void getRemaining_negative() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn("100");

        int remaining = rateLimiterService.getRemaining("USER_test");

        assertEquals(0, remaining);
    }

    @Test
    @DisplayName("getWindowMs return 60000 ms")
    void getWindowMs() {
        assertEquals(60000L, rateLimiterService.getWindowMs());
    }
}