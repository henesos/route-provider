package com.aviation.routeprovider.application.config;

import java.time.Duration;

public interface CacheConfiguration {

    Duration getRouteCacheTtl();

    Duration getTransportationCacheTtl();

    Duration getLocationCacheTtl();

    Duration getGraphCacheTtl();
}
