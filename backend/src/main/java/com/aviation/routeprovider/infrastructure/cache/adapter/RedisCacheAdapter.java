package com.aviation.routeprovider.infrastructure.cache.adapter;

import com.aviation.routeprovider.application.port.out.CachePort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheAdapter implements CachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final long MAX_SCAN_COUNT = 10000;

    public RedisCacheAdapter(RedisTemplate<String, Object> redisTemplate,
                              ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            T converted = objectMapper.convertValue(value, type);
            return Optional.ofNullable(converted);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to convert cached value for key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            T converted = objectMapper.convertValue(value, typeReference);
            return Optional.ofNullable(converted);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to convert cached value for key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void evictByPattern(String pattern) {
        scanAndDelete(pattern);
    }

    private void scanAndDelete(String pattern) {
        try {
            List<String> keysToDelete = scanKeys(pattern);
            
            if (!keysToDelete.isEmpty()) {
                Long deleted = redisTemplate.delete(keysToDelete);
                log.debug("Evicted {} keys matching pattern {}", deleted, pattern);
            }
            
        } catch (Exception e) {
            log.error("Failed to evict keys by pattern {}: {}", pattern, e.getMessage());
            fallbackEvictByPattern(pattern);
        }
    }

    private List<String> scanKeys(String pattern) {
        List<String> keysToDelete = new ArrayList<>();
        
        ScanOptions options = ScanOptions.scanOptions()
            .match(pattern)
            .count(100)
            .build();
        
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options)) {
            
            long count = 0;
            while (cursor.hasNext() && count < MAX_SCAN_COUNT) {
                byte[] keyBytes = cursor.next();
                keysToDelete.add(new String(keyBytes));
                count++;
            }
            
            if (count >= MAX_SCAN_COUNT) {
                log.warn("SCAN reached max count {} for pattern {}. Some keys may not be evicted.", 
                    MAX_SCAN_COUNT, pattern);
            }
        }
        
        return keysToDelete;
    }

    private void fallbackEvictByPattern(String pattern) {
        log.warn("Using KEYS command as fallback for pattern {}. This may impact performance.", pattern);
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean exists(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }
}
