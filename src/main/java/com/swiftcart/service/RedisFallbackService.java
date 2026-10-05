package com.swiftcart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RedisFallbackService {

    private static final Logger log = LoggerFactory.getLogger(RedisFallbackService.class);
    private static final long COOLDOWN_MILLIS = 30_000L; // 30 seconds circuit breaker cooldown

    private final StringRedisTemplate redisTemplate;
    private final Map<String, String> inMemoryStore = new ConcurrentHashMap<>();
    private final Map<String, Long> inMemoryExpiry = new ConcurrentHashMap<>();

    private volatile long lastFailureTime = 0L;
    private final AtomicBoolean redisDownLogged = new AtomicBoolean(false);

    public RedisFallbackService(java.util.Optional<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate.orElse(null);
        if (this.redisTemplate == null) {
            log.info("Redis template is not available. Using local in-memory fallback store.");
        }
    }

    private boolean isRedisAvailable() {
        if (redisTemplate == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        // If within cooldown period after a connection failure, bypass network call immediately
        return (now - lastFailureTime) >= COOLDOWN_MILLIS;
    }

    private void handleRedisFailure(String operation, Exception e) {
        lastFailureTime = System.currentTimeMillis();
        if (redisDownLogged.compareAndSet(false, true)) {
            log.warn("Redis is currently unavailable (operation: {}). Entering {}ms cooldown; degrading gracefully to in-memory store. Cause: {}",
                    operation, COOLDOWN_MILLIS, e.getMessage());
        } else {
            log.debug("Redis operation {} failed: {}", operation, e.getMessage());
        }
    }

    private void handleRedisSuccess() {
        if (redisDownLogged.compareAndSet(true, false)) {
            log.info("Redis connection restored. Resuming normal Redis operations.");
        }
    }

    public void set(String key, String value, Duration ttl) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(key, value, ttl);
                handleRedisSuccess();
                return;
            } catch (Exception e) {
                handleRedisFailure("set", e);
            }
        }
        inMemoryStore.put(key, value);
        inMemoryExpiry.put(key, System.currentTimeMillis() + ttl.toMillis());
    }

    public String get(String key) {
        if (isRedisAvailable()) {
            try {
                String val = redisTemplate.opsForValue().get(key);
                handleRedisSuccess();
                return val;
            } catch (Exception e) {
                handleRedisFailure("get", e);
            }
        }
        Long expiry = inMemoryExpiry.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            inMemoryStore.remove(key);
            inMemoryExpiry.remove(key);
            return null;
        }
        return inMemoryStore.get(key);
    }

    public void delete(String key) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.delete(key);
                handleRedisSuccess();
                return;
            } catch (Exception e) {
                handleRedisFailure("delete", e);
            }
        }
        inMemoryStore.remove(key);
        inMemoryExpiry.remove(key);
    }

    public Long incrementAndExpire(String key, Duration ttl) {
        if (isRedisAvailable()) {
            try {
                Long val = redisTemplate.opsForValue().increment(key);
                if (val != null && val.equals(1L)) {
                    redisTemplate.expire(key, ttl);
                }
                handleRedisSuccess();
                return val;
            } catch (Exception e) {
                handleRedisFailure("increment", e);
            }
        }
        
        Long expiry = inMemoryExpiry.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            inMemoryStore.remove(key);
            inMemoryExpiry.remove(key);
        }
        
        String current = inMemoryStore.get(key);
        long next = 1;
        if (current != null) {
            try {
                next = Long.parseLong(current) + 1;
            } catch (NumberFormatException ignored) {}
        }
        inMemoryStore.put(key, String.valueOf(next));
        if (current == null) {
            inMemoryExpiry.put(key, System.currentTimeMillis() + ttl.toMillis());
        }
        return next;
    }
}
