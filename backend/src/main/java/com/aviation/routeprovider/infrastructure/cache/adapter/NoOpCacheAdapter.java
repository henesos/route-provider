package com.aviation.routeprovider.infrastructure.cache.adapter;

import com.aviation.routeprovider.application.port.out.CachePort;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnMissingBean(RedisCacheAdapter.class)
public class NoOpCacheAdapter implements CachePort {

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        return Optional.empty();
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
    }

    @Override
    public void evict(String key) {
    }

    @Override
    public void evictByPattern(String pattern) {
    }

    @Override
    public boolean exists(String key) {
        return false;
    }
}
