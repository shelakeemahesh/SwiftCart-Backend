package com.swiftcart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RedisFallbackServiceTest {

    private RedisFallbackService fallbackServiceWithoutRedis;
    private StringRedisTemplate mockRedisTemplate;
    private ValueOperations<String, String> mockValueOperations;
    private RedisFallbackService fallbackServiceWithRedis;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        fallbackServiceWithoutRedis = new RedisFallbackService(Optional.empty());

        mockRedisTemplate = mock(StringRedisTemplate.class);
        mockValueOperations = mock(ValueOperations.class);
        when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOperations);
        fallbackServiceWithRedis = new RedisFallbackService(Optional.of(mockRedisTemplate));
    }

    @Test
    void testIncrementAndExpire_WithoutRedis_UsesInMemoryStore() {
        String key = "rate:public:127.0.0.1:100";
        Long first = fallbackServiceWithoutRedis.incrementAndExpire(key, Duration.ofSeconds(60));
        Long second = fallbackServiceWithoutRedis.incrementAndExpire(key, Duration.ofSeconds(60));

        assertEquals(1L, first);
        assertEquals(2L, second);
    }

    @Test
    void testSetGetDelete_WithoutRedis_UsesInMemoryStore() {
        String key = "test:key";
        fallbackServiceWithoutRedis.set(key, "hello", Duration.ofSeconds(60));
        assertEquals("hello", fallbackServiceWithoutRedis.get(key));

        fallbackServiceWithoutRedis.delete(key);
        assertNull(fallbackServiceWithoutRedis.get(key));
    }

    @Test
    void testIncrementAndExpire_WhenRedisFails_DegradesToInMemoryAndEntersCooldown() {
        String key = "rate:public:192.168.1.1:100";
        when(mockValueOperations.increment(anyString()))
                .thenThrow(new RedisConnectionFailureException("Unable to connect to Redis"));

        // First call fails Redis and enters cooldown
        Long first = fallbackServiceWithRedis.incrementAndExpire(key, Duration.ofSeconds(60));
        assertEquals(1L, first);

        // Second call should bypass Redis completely during the 30-second cooldown
        Long second = fallbackServiceWithRedis.incrementAndExpire(key, Duration.ofSeconds(60));
        assertEquals(2L, second);

        // Verify Redis was only attempted ONCE, not twice
        verify(mockValueOperations, times(1)).increment(anyString());
    }

    @Test
    void testSetAndGet_WhenRedisFails_DegradesToInMemoryWithoutFailing() {
        when(mockValueOperations.get(anyString()))
                .thenThrow(new RedisConnectionFailureException("Connection refused"));
        doThrow(new RedisConnectionFailureException("Connection refused"))
                .when(mockValueOperations).set(anyString(), anyString(), any(Duration.class));

        fallbackServiceWithRedis.set("sample", "val", Duration.ofSeconds(60));
        assertEquals("val", fallbackServiceWithRedis.get("sample"));
    }
}
