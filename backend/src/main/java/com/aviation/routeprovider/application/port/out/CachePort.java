package com.aviation.routeprovider.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

    <T> Optional<T> get(String key, Class<T> type);

    <T> Optional<T> get(String key, com.fasterxml.jackson.core.type.TypeReference<T> typeReference);

    void set(String key, Object value, Duration ttl);

    void evict(String key);

    void evictByPattern(String pattern);

    boolean exists(String key);
}
