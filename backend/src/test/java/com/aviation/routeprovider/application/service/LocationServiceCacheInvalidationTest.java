package com.aviation.routeprovider.application.service;

import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.in.CreateLocationCommand;
import com.aviation.routeprovider.application.port.in.UpdateLocationCommand;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.application.port.out.LocationRepository;
import com.aviation.routeprovider.application.port.out.TransportationRepository;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.valueobject.LocationCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LocationService cache invalidation behavior.
 * 
 * CRITICAL TEST: Verifies that graph cache is invalidated when location changes.
 * 
 * Background:
 * - Graph cache stores Transportation objects which contain Location objects
 * - If location is updated but graph cache is not invalidated, routes will show stale location data
 * - This was a bug fixed in V18 - location update must invalidate graph cache
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceCacheInvalidationTest {

    @Mock
    private LocationRepository locationRepository;
    
    @Mock
    private TransportationRepository transportationRepository;
    
    @Mock
    private CachePort cachePort;
    
    @Mock
    private CacheConfiguration cacheConfiguration;
    
    private LocationService locationService;
    
    private Location existingLocation;
    
    @BeforeEach
    void setUp() {
        when(cacheConfiguration.getLocationCacheTtl()).thenReturn(Duration.ofMinutes(60));
        
        locationService = new LocationService(
            locationRepository, transportationRepository, cachePort, cacheConfiguration
        );
        
        existingLocation = Location.reconstruct(
            1L, "Istanbul Airport", "Turkey", "Istanbul", new LocationCode("IST")
        );
    }
    
    @Nested
    @DisplayName("Graph Cache Invalidation")
    class GraphCacheInvalidationTests {
        
        @Test
        @DisplayName("Should invalidate graph cache when location is created")
        void shouldInvalidateGraphCacheOnCreate() {
            // Given
            when(locationRepository.existsByLocationCode("SAW")).thenReturn(false);
            when(locationRepository.save(any())).thenReturn(
                Location.reconstruct(2L, "Sabiha Gokcen", "Turkey", "Istanbul", new LocationCode("SAW"))
            );
            
            CreateLocationCommand command = new CreateLocationCommand(
                "Sabiha Gokcen", "Turkey", "Istanbul", "SAW"
            );
            
            // When
            locationService.createLocation(command);
            
            // Then - verify graph cache is invalidated
            verify(cachePort).evictByPattern("graph:*");
        }
        
        @Test
        @DisplayName("Should invalidate graph cache when location is updated")
        void shouldInvalidateGraphCacheOnUpdate() {
            // Given
            when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
            when(locationRepository.save(any())).thenReturn(existingLocation);
            
            UpdateLocationCommand command = new UpdateLocationCommand(
                "Istanbul Grand Airport", "Turkey", "Istanbul"
            );
            
            // When
            locationService.updateLocation(1L, command);
            
            // Then - verify graph cache is invalidated
            verify(cachePort).evictByPattern("graph:*");
        }
        
        @Test
        @DisplayName("Should invalidate graph cache when location is deleted")
        void shouldInvalidateGraphCacheOnDelete() {
            // Given
            when(locationRepository.existsById(1L)).thenReturn(true);
            when(transportationRepository.countByLocationId(1L)).thenReturn(0L);
            
            // When
            locationService.deleteLocation(1L);
            
            // Then - verify graph cache is invalidated
            verify(cachePort).evictByPattern("graph:*");
        }
        
        @Test
        @DisplayName("Should invalidate all related caches on location update")
        void shouldInvalidateAllRelatedCachesOnUpdate() {
            // Given
            when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
            when(locationRepository.save(any())).thenReturn(existingLocation);
            
            UpdateLocationCommand command = new UpdateLocationCommand(
                "Updated Name", "Turkey", "Istanbul"
            );
            
            // When
            locationService.updateLocation(1L, command);
            
            // Then - verify ALL caches are invalidated in correct order
            // Locations cache
            verify(cachePort).evictByPattern("locations:*");
            // Routes cache (depends on location data)
            verify(cachePort).evictByPattern("routes:*");
            // Graph cache (stores Transportation objects with Location data)
            verify(cachePort).evictByPattern("graph:*");
            // Individual location cache
            verify(cachePort).evict("locations:1");
        }
        
        @Test
        @DisplayName("Should not invalidate graph cache on read operations")
        void shouldNotInvalidateGraphCacheOnRead() {
            // Given
            when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
            when(cachePort.get(anyString(), eq(Location.class))).thenReturn(Optional.empty());
            
            // When
            locationService.getLocation(1L);
            
            // Then - no cache eviction should happen
            verify(cachePort, never()).evictByPattern(anyString());
        }
    }
    
    @Nested
    @DisplayName("Cache Invalidation Order")
    class CacheInvalidationOrderTests {
        
        @Test
        @DisplayName("Should evict caches in correct order on location delete")
        void shouldEvictCachesInCorrectOrderOnDelete() {
            // Given
            when(locationRepository.existsById(1L)).thenReturn(true);
            when(transportationRepository.countByLocationId(1L)).thenReturn(0L);
            
            ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
            
            // When
            locationService.deleteLocation(1L);
            
            // Then - verify the order of cache invalidation
            verify(cachePort, times(3)).evictByPattern(patternCaptor.capture());
            
            var patterns = patternCaptor.getAllValues();
            assertEquals("locations:*", patterns.get(0));
            assertEquals("routes:*", patterns.get(1));
            assertEquals("graph:*", patterns.get(2));
        }
    }
}
