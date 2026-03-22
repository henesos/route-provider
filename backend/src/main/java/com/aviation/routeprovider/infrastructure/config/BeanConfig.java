package com.aviation.routeprovider.infrastructure.config;

import com.aviation.routeprovider.domain.service.RouteEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
public class BeanConfig {

    @Bean
    public RouteEngine routeEngine() {
        return new RouteEngine();
    }
}
