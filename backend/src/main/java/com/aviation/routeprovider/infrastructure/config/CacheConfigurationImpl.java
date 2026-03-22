package com.aviation.routeprovider.infrastructure.config;

import com.aviation.routeprovider.application.config.CacheConfiguration;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CacheConfigurationImpl implements CacheConfiguration {

    private final AppConfig appConfig;

    public CacheConfigurationImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public Duration getRouteCacheTtl() {
        return Duration.ofMinutes(appConfig.getRoute().getCacheTtlMinutes());
    }

    @Override
    public Duration getTransportationCacheTtl() {
        return Duration.ofHours(1);
    }

    @Override
    public Duration getLocationCacheTtl() {
        return Duration.ofHours(1);
    }

    @Override
    public Duration getGraphCacheTtl() {
        return Duration.ofMinutes(appConfig.getGraphCache().getTtlMinutes());
    }
}
