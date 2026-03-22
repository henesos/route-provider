package com.aviation.routeprovider.application.service;

import com.aviation.routeprovider.api.rest.dto.response.RouteResponse;
import com.aviation.routeprovider.api.rest.mapper.RouteMapper;
import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.in.RouteSearchQuery;
import com.aviation.routeprovider.application.port.in.RouteSearchUseCase;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.application.port.out.LocationRepository;
import com.aviation.routeprovider.application.port.out.TransportationRepository;
import com.aviation.routeprovider.domain.exception.LocationNotFoundException;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.Route;
import com.aviation.routeprovider.domain.service.RouteEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RouteSearchService implements RouteSearchUseCase {

    private static final String CACHE_KEY_PREFIX = "routes:";

    private static final TypeReference<List<RouteResponse>> ROUTE_LIST_TYPE = new TypeReference<>() {};
    
    private final LocationRepository locationRepository;
    private final TransportationRepository transportationRepository;
    private final CachePort cachePort;
    private final RouteEngine routeEngine;
    private final RouteMapper routeMapper;
    private final Duration cacheTtl;

    public RouteSearchService(
            LocationRepository locationRepository,
            TransportationRepository transportationRepository,
            CachePort cachePort,
            RouteEngine routeEngine,
            RouteMapper routeMapper,
            CacheConfiguration cacheConfiguration) {
        this.locationRepository = locationRepository;
        this.transportationRepository = transportationRepository;
        this.cachePort = cachePort;
        this.routeEngine = routeEngine;
        this.routeMapper = routeMapper;
        this.cacheTtl = cacheConfiguration.getRouteCacheTtl();
    }
    
    @Override
    public List<RouteResponse> searchRoutes(RouteSearchQuery query) {
        String cacheKey = buildCacheKey(query);

        List<RouteResponse> cached = cachePort.get(cacheKey, ROUTE_LIST_TYPE).orElse(null);
        if (cached != null) {
            return cached;
        }

        Location origin = locationRepository.findById(query.originLocationId())
            .orElseThrow(() -> new LocationNotFoundException(query.originLocationId()));
        
        Location destination = locationRepository.findById(query.destinationLocationId())
            .orElseThrow(() -> new LocationNotFoundException(query.destinationLocationId()));

        DayOfWeek travelDay = query.travelDate().getDayOfWeek();
        Map<Long, List<Transportation>> adjacencyMap = 
            transportationRepository.loadAdjacencyMap(travelDay);

        List<Route> routes = routeEngine.findRoutes(
            adjacencyMap, origin, destination, query.travelDate()
        );

        List<RouteResponse> response = routeMapper.toResponseList(routes);

        cachePort.set(cacheKey, response, cacheTtl);
        
        return response;
    }
    
    private String buildCacheKey(RouteSearchQuery query) {
        return String.format("%s%d:%d:%s",
            CACHE_KEY_PREFIX,
            query.originLocationId(),
            query.destinationLocationId(),
            query.travelDate()
        );
    }
}
