package com.swiftcart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisFallbackService {

    private static final Logger log = LoggerFactory.getLogger(RedisFallbackService.class);

    private final StringRedisTemplate redisTemplate;
    private final Map<String, String> inMemoryStore = new ConcurrentHashMap<>();
    private final Map<String, Long> inMemoryExpiry = new ConcurrentHashMap<>();

    public RedisFallbackService(java.util.Optional<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate.orElse(null);
        if (this.redisTemplate == null) {
            log.info("Redis template is not available. Using local in-memory fallback store.");
        }
    }

    public void set(String key, String value, Duration ttl) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttl);
                return;
            } catch (Exception e) {
                log.warn("Redis write failed for key: {}. Falling back to in-memory store. Error: {}", key, e.getMessage());
            }
        }
        inMemoryStore.put(key, value);
        inMemoryExpiry.put(key, System.currentTimeMillis() + ttl.toMillis());
    }

    public String get(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("Redis read failed for key: {}. Falling back to in-memory store. Error: {}", key, e.getMessage());
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
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
                return;
            } catch (Exception e) {
                log.warn("Redis delete failed for key: {}. Falling back to in-memory store. Error: {}", key, e.getMessage());
            }
        }
        inMemoryStore.remove(key);
        inMemoryExpiry.remove(key);
    }

    public Long incrementAndExpire(String key, Duration ttl) {
        if (redisTemplate != null) {
            try {
                Long val = redisTemplate.opsForValue().increment(key);
                if (val != null && val.equals(1L)) {
                    redisTemplate.expire(key, ttl);
                }
                return val;
            } catch (Exception e) {
                log.warn("Redis increment failed for key: {}. Falling back to in-memory store. Error: {}", key, e.getMessage());
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
