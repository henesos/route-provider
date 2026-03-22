package com.aviation.routeprovider.infrastructure.persistence.adapter;

import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.application.port.out.TransportationRepository;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.entity.TransportationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.mapper.TransportationPersistenceMapper;
import com.aviation.routeprovider.infrastructure.persistence.repository.LocationJpaRepository;
import com.aviation.routeprovider.infrastructure.persistence.repository.TransportationJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
public class TransportationPersistenceAdapter implements TransportationRepository {
    
    private static final String GRAPH_CACHE_PREFIX = "graph:";
    private static final TypeReference<Map<Long, List<Transportation>>> ADJACENCY_MAP_TYPE = 
        new TypeReference<>() {};
    
    private final TransportationJpaRepository jpaRepository;
    private final LocationJpaRepository locationJpaRepository;
    private final TransportationPersistenceMapper mapper;
    private final CachePort cachePort;
    private final Duration graphCacheTtl;
    
    public TransportationPersistenceAdapter(
            TransportationJpaRepository jpaRepository,
            LocationJpaRepository locationJpaRepository,
            TransportationPersistenceMapper mapper,
            CachePort cachePort,
            CacheConfiguration cacheConfiguration) {
        this.jpaRepository = jpaRepository;
        this.locationJpaRepository = locationJpaRepository;
        this.mapper = mapper;
        this.cachePort = cachePort;
        this.graphCacheTtl = cacheConfiguration.getGraphCacheTtl();
    }
    
    @Override
    public Transportation save(Transportation transportation) {
        TransportationJpaEntity jpaEntity = mapper.toJpaEntity(transportation);

        LocationJpaEntity origin = locationJpaRepository.getReferenceById(
            transportation.getOriginLocation().getId());
        LocationJpaEntity destination = locationJpaRepository.getReferenceById(
            transportation.getDestinationLocation().getId());
        
        jpaEntity.setOriginLocation(origin);
        jpaEntity.setDestinationLocation(destination);
        
        TransportationJpaEntity saved = jpaRepository.save(jpaEntity);

        invalidateGraphCache();
        
        return mapper.toDomain(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Transportation> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Transportation> findAll(Pageable pageable) {
        return jpaRepository.findAllWithLocations(pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
        invalidateGraphCache();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<Transportation>> loadAdjacencyMap(DayOfWeek day) {
        if (day == null) {
            return loadAdjacencyMapFromDb(null);
        }

        String cacheKey = buildGraphCacheKey(day);
        Optional<Map<Long, List<Transportation>>> cached = 
            cachePort.get(cacheKey, ADJACENCY_MAP_TYPE);
        
        if (cached.isPresent()) {
            Map<Long, List<Transportation>> cachedMap = cached.get();
            if (cachedMap != null) {
                return cachedMap;
            }
        }

        Map<Long, List<Transportation>> adjacencyMap = loadAdjacencyMapFromDb(day);

         if (adjacencyMap != null && !adjacencyMap.isEmpty()) {
            cachePort.set(cacheKey, adjacencyMap, graphCacheTtl);
        }

        return adjacencyMap != null ? adjacencyMap : new HashMap<>();
    }

    private Map<Long, List<Transportation>> loadAdjacencyMapFromDb(DayOfWeek day) {
        List<TransportationJpaEntity> transportations;

        if (day != null) {
            int dayNumber = OperatingDays.toDayNumber(day);
            List<Long> ids = jpaRepository.findIdsByOperatingDay(dayNumber);
            
            if (ids.isEmpty()) {
                return new HashMap<>();
            }
            
            transportations = jpaRepository.findByIdsWithLocations(ids);
        } else {
            transportations = jpaRepository.findAllWithLocations();
        }

        // Group by origin location
        Map<Long, List<Transportation>> adjacencyMap = new HashMap<>();
        
        for (TransportationJpaEntity jpaEntity : transportations) {
            Transportation transportation = mapper.toDomain(jpaEntity);
            Long originId = transportation.getOriginLocation().getId();
            
            adjacencyMap.computeIfAbsent(originId, k -> new java.util.ArrayList<>())
                .add(transportation);
        }
        
        return adjacencyMap;
    }

    private void invalidateGraphCache() {
        cachePort.evictByPattern(GRAPH_CACHE_PREFIX + "*");
    }

    private String buildGraphCacheKey(DayOfWeek day) {
        return GRAPH_CACHE_PREFIX + OperatingDays.toDayNumber(day);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByLocationId(Long locationId) {
        return jpaRepository.countByOriginLocationId(locationId) +
               jpaRepository.countByDestinationLocationId(locationId);
    }
}
