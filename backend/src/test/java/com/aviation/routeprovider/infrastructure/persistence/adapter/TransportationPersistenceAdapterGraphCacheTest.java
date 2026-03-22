package com.aviation.routeprovider.infrastructure.persistence.adapter;

import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.LocationCode;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.entity.TransportationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.mapper.TransportationPersistenceMapper;
import com.aviation.routeprovider.infrastructure.persistence.repository.LocationJpaRepository;
import com.aviation.routeprovider.infrastructure.persistence.repository.TransportationJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportationPersistenceAdapterGraphCacheTest {

    @Mock
    private TransportationJpaRepository jpaRepository;
    
    @Mock
    private LocationJpaRepository locationJpaRepository;
    
    @Mock
    private TransportationPersistenceMapper mapper;
    
    @Mock
    private CachePort cachePort;
    
    @Mock
    private CacheConfiguration cacheConfiguration;
    
    private TransportationPersistenceAdapter adapter;
    
    private Location istanbul;
    private Location london;
    private Transportation flight;
    
    @BeforeEach
    void setUp() {
        when(cacheConfiguration.getGraphCacheTtl()).thenReturn(Duration.ofMinutes(60));
        
        adapter = new TransportationPersistenceAdapter(
            jpaRepository, locationJpaRepository, mapper, cachePort, cacheConfiguration
        );
        
        istanbul = Location.reconstruct(1L, "Istanbul Airport", "Turkey", "Istanbul", new LocationCode("IST"));
        london = Location.reconstruct(2L, "London Heathrow", "UK", "London", new LocationCode("LHR"));
        flight = Transportation.reconstruct(1L, istanbul, london, TransportationType.FLIGHT, new OperatingDays(1, 2, 3, 4, 5, 6, 7));
    }
    
    @Nested
    @DisplayName("Graph Cache Load Behavior")
    class LoadBehaviorTests {
        
        @Test
        @DisplayName("Should return cached adjacency map on cache hit")
        void shouldReturnCachedOnHit() {
            // Given
            DayOfWeek monday = DayOfWeek.MONDAY;
            Map<Long, List<Transportation>> cachedMap = Map.of(
                1L, List.of(flight)
            );
            
            when(cachePort.get(eq("graph:1"), any(TypeReference.class)))
                .thenReturn(Optional.of(cachedMap));
            
            // When
            Map<Long, List<Transportation>> result = adapter.loadAdjacencyMap(monday);
            
            // Then
            assertSame(cachedMap, result);
            // On cache hit, DB methods should NOT be called
            verify(jpaRepository, never()).findIdsByOperatingDay(anyInt());
            verify(jpaRepository, never()).findByIdsWithLocations(anyList());
            verify(jpaRepository, never()).findAllWithLocations();
        }
        
        @Test
        @DisplayName("Should load from DB and cache on cache miss")
        void shouldLoadFromDbAndCacheOnMiss() {
            // Given
            DayOfWeek monday = DayOfWeek.MONDAY;
            TransportationJpaEntity jpaEntity = mock(TransportationJpaEntity.class);
            
            when(cachePort.get(eq("graph:1"), any(TypeReference.class)))
                .thenReturn(Optional.empty());
            when(jpaRepository.findIdsByOperatingDay(1))
                .thenReturn(List.of(1L));
            when(jpaRepository.findByIdsWithLocations(List.of(1L)))
                .thenReturn(List.of(jpaEntity));
            when(mapper.toDomain(jpaEntity)).thenReturn(flight);
            
            // When
            Map<Long, List<Transportation>> result = adapter.loadAdjacencyMap(monday);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(List.of(flight), result.get(1L));
            
            // Verify two-step query was called
            verify(jpaRepository).findIdsByOperatingDay(1);
            verify(jpaRepository).findByIdsWithLocations(List.of(1L));
            verify(jpaRepository, never()).findAllWithLocations();
            
            // Verify caching happened
            verify(cachePort).set(eq("graph:1"), any(), eq(Duration.ofMinutes(60)));
        }
        
        @Test
        @DisplayName("Should use correct cache key for each day of week")
        void shouldUseCorrectCacheKeyForDayOfWeek() {
            // Given - All cache hits
            Map<Long, List<Transportation>> cachedMap = Map.of(1L, List.of(flight));
            
            when(cachePort.get(anyString(), any(TypeReference.class)))
                .thenReturn(Optional.of(cachedMap));
            
            // When/Then - Verify each day maps to correct key
            // Monday = 1, Tuesday = 2, ..., Sunday = 7
            adapter.loadAdjacencyMap(DayOfWeek.MONDAY);
            verify(cachePort).get(eq("graph:1"), any(TypeReference.class));
            
            adapter.loadAdjacencyMap(DayOfWeek.TUESDAY);
            verify(cachePort).get(eq("graph:2"), any(TypeReference.class));
            
            adapter.loadAdjacencyMap(DayOfWeek.SUNDAY);
            verify(cachePort).get(eq("graph:7"), any(TypeReference.class));
        }
        
        @Test
        @DisplayName("Should not cache empty adjacency map")
        void shouldNotCacheEmptyMap() {
            // Given
            DayOfWeek monday = DayOfWeek.MONDAY;
            
            when(cachePort.get(eq("graph:1"), any(TypeReference.class)))
                .thenReturn(Optional.empty());
            // findIdsByOperatingDay returns empty list - no transportations for this day
            when(jpaRepository.findIdsByOperatingDay(1))
                .thenReturn(List.of());
            
            // When
            Map<Long, List<Transportation>> result = adapter.loadAdjacencyMap(monday);
            
            // Then
            assertTrue(result.isEmpty());
            // findByIdsWithLocations should NOT be called when IDs are empty
            verify(jpaRepository, never()).findByIdsWithLocations(anyList());
            // Empty maps should not be cached
            verify(cachePort, never()).set(anyString(), any(), any(Duration.class));
        }
        
        @Test
        @DisplayName("Should not cache when day is null (load all)")
        void shouldNotCacheWhenDayIsNull() {
            // Given
            TransportationJpaEntity jpaEntity = mock(TransportationJpaEntity.class);
            
            when(jpaRepository.findAllWithLocations())
                .thenReturn(List.of(jpaEntity));
            when(mapper.toDomain(jpaEntity)).thenReturn(flight);
            
            // When
            Map<Long, List<Transportation>> result = adapter.loadAdjacencyMap(null);
            
            // Then
            assertEquals(1, result.size());
            // For null day, cache should not be accessed
            verify(cachePort, never()).get(anyString(), any(TypeReference.class));
            verify(cachePort, never()).set(anyString(), any(), any(Duration.class));
            // Should use findAllWithLocations, not day-filtered queries
            verify(jpaRepository).findAllWithLocations();
            verify(jpaRepository, never()).findIdsByOperatingDay(anyInt());
        }
    }
    
    @Nested
    @DisplayName("Graph Cache Invalidation")
    class InvalidationTests {
        
        @Test
        @DisplayName("Should invalidate graph cache on save")
        void shouldInvalidateOnSave() {
            // Given
            Transportation newTransportation = Transportation.create(
                istanbul, london, TransportationType.FLIGHT, new OperatingDays(1, 2, 3)
            );
            TransportationJpaEntity jpaEntity = new TransportationJpaEntity();
            LocationJpaEntity originJpa = mock(LocationJpaEntity.class);
            LocationJpaEntity destJpa = mock(LocationJpaEntity.class);
            
            when(locationJpaRepository.getReferenceById(1L)).thenReturn(originJpa);
            when(locationJpaRepository.getReferenceById(2L)).thenReturn(destJpa);
            when(mapper.toJpaEntity(newTransportation)).thenReturn(jpaEntity);
            when(jpaRepository.save(jpaEntity)).thenReturn(jpaEntity);
            when(mapper.toDomain(jpaEntity)).thenReturn(flight);
            
            // When
            adapter.save(newTransportation);
            
            // Then
            verify(cachePort).evictByPattern("graph:*");
        }
        
        @Test
        @DisplayName("Should invalidate graph cache on delete")
        void shouldInvalidateOnDelete() {
            // When
            adapter.deleteById(1L);
            
            // Then
            verify(cachePort).evictByPattern("graph:*");
        }
    }
}
