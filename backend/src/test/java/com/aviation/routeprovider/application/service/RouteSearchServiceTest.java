package com.aviation.routeprovider.application.service;

import com.aviation.routeprovider.api.rest.dto.response.RouteResponse;
import com.aviation.routeprovider.api.rest.mapper.RouteMapper;
import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.in.RouteSearchQuery;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.application.port.out.LocationRepository;
import com.aviation.routeprovider.application.port.out.TransportationRepository;
import com.aviation.routeprovider.domain.exception.LocationNotFoundException;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.LocationCode;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.domain.model.valueobject.Route;
import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import com.aviation.routeprovider.domain.service.RouteEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteSearchServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private CachePort cachePort;

    @Mock
    private RouteEngine routeEngine;

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private CacheConfiguration cacheConfiguration;

    private RouteSearchService routeSearchService;

    private Location origin;
    private Location destination;
    private RouteSearchQuery query;
    private RouteResponse routeResponse;

    @BeforeEach
    void setUp() {
        when(cacheConfiguration.getRouteCacheTtl()).thenReturn(Duration.ofMinutes(15));
        
        // FIX [ISSUE 2]: RouteMapper is now a required dependency
        routeSearchService = new RouteSearchService(
            locationRepository, transportationRepository, cachePort, routeEngine, routeMapper, cacheConfiguration
        );

        origin = Location.reconstruct(1L, "Istanbul Airport", "Turkey", "Istanbul", new LocationCode("IST"));
        destination = Location.reconstruct(2L, "London Heathrow", "UK", "London", new LocationCode("LHR"));
        query = new RouteSearchQuery(1L, 2L, LocalDate.of(2024, 3, 18)); // Monday
        
        // Create a sample RouteResponse for testing
        routeResponse = new RouteResponse(
            new RouteResponse.LocationSummary(1L, "Istanbul Airport", "IST", "Istanbul", "Turkey"),
            new RouteResponse.LocationSummary(2L, "London Heathrow", "LHR", "London", "UK"),
            1,
            List.of(new RouteResponse.TransportationSegment(
                "FLIGHT",
                new RouteResponse.LocationSummary(1L, "Istanbul Airport", "IST", "Istanbul", "Turkey"),
                new RouteResponse.LocationSummary(2L, "London Heathrow", "LHR", "London", "UK"),
                new int[]{1, 2, 3, 4, 5, 6, 7}
            ))
        );
    }

    @Test
    @DisplayName("Should return routes from cache when available")
    void shouldReturnRoutesFromCache() {
        List<RouteResponse> cachedRoutes = List.of(routeResponse);

        when(cachePort.get(anyString(), any(TypeReference.class))).thenReturn(Optional.of(cachedRoutes));

        // When
        List<RouteResponse> result = routeSearchService.searchRoutes(query);

        // Then
        assertEquals(cachedRoutes, result);
        verify(locationRepository, never()).findById(any());
        verify(transportationRepository, never()).loadAdjacencyMap(any());
    }

    @Test
    @DisplayName("Should fetch from repository when cache miss")
    void shouldFetchFromRepositoryWhenCacheMiss() {
        // Given
        Transportation transportation = Transportation.reconstruct(
            1L, origin, destination, TransportationType.FLIGHT, new OperatingDays(1, 2, 3, 4, 5, 6, 7)
        );
        Route route = new Route(List.of(transportation));
        List<Route> routes = List.of(route);
        List<RouteResponse> responseList = List.of(routeResponse);

        when(cachePort.get(anyString(), any(TypeReference.class))).thenReturn(Optional.empty());
        when(locationRepository.findById(1L)).thenReturn(Optional.of(origin));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(transportationRepository.loadAdjacencyMap(DayOfWeek.MONDAY)).thenReturn(Map.of());
        when(routeEngine.findRoutes(any(), any(), any(), any())).thenReturn(routes);
        when(routeMapper.toResponseList(routes)).thenReturn(responseList);

        // When
        List<RouteResponse> result = routeSearchService.searchRoutes(query);

        // Then
        assertEquals(responseList, result);
        // FIX [ISSUE 2]: Verify DTOs are cached, not domain objects
        verify(cachePort).set(anyString(), eq(responseList), any(Duration.class));
        verify(routeMapper).toResponseList(routes);
    }

    @Test
    @DisplayName("Should throw exception when origin not found")
    void shouldThrowExceptionWhenOriginNotFound() {
        // Given
        when(cachePort.get(anyString(), any(TypeReference.class))).thenReturn(Optional.empty());
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(LocationNotFoundException.class, () -> routeSearchService.searchRoutes(query));
    }

    @Test
    @DisplayName("Should throw exception when destination not found")
    void shouldThrowExceptionWhenDestinationNotFound() {
        // Given
        when(cachePort.get(anyString(), any(TypeReference.class))).thenReturn(Optional.empty());
        when(locationRepository.findById(1L)).thenReturn(Optional.of(origin));
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(LocationNotFoundException.class, () -> routeSearchService.searchRoutes(query));
    }
}
