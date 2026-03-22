package com.aviation.routeprovider.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "application")
public class AppConfig {
    
    private RouteConfig route = new RouteConfig();
    private GraphCacheConfig graphCache = new GraphCacheConfig();
    
    @Data
    public static class RouteConfig {
        private int cacheTtlMinutes;
    }
    
    @Data
    public static class GraphCacheConfig {
        private int ttlMinutes;
    }
}
