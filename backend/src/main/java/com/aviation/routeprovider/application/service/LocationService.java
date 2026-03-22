package com.aviation.routeprovider.application.service;

import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.in.CreateLocationCommand;
import com.aviation.routeprovider.application.port.in.LocationUseCase;
import com.aviation.routeprovider.application.port.in.UpdateLocationCommand;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.application.port.out.LocationRepository;
import com.aviation.routeprovider.application.port.out.TransportationRepository;
import com.aviation.routeprovider.domain.exception.DomainException;
import com.aviation.routeprovider.domain.exception.LocationNotFoundException;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.valueobject.LocationCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
public class LocationService implements LocationUseCase {

    private static final String CACHE_KEY_PREFIX = "locations:";

    private final LocationRepository locationRepository;
    private final TransportationRepository transportationRepository;
    private final CachePort cachePort;
    private final Duration cacheTtl;

    public LocationService(
            LocationRepository locationRepository,
            TransportationRepository transportationRepository,
            CachePort cachePort,
            CacheConfiguration cacheConfiguration) {
        this.locationRepository = locationRepository;
        this.transportationRepository = transportationRepository;
        this.cachePort = cachePort;
        this.cacheTtl = cacheConfiguration.getLocationCacheTtl();
    }
    
    @Override
    public Location createLocation(CreateLocationCommand command) {
        // Check if location code already exists
        if (locationRepository.existsByLocationCode(command.locationCode())) {
            throw new DomainException(
                "Location code already exists: " + command.locationCode());
        }

        Location location = Location.create(
            command.name(),
            command.country(),
            command.city(),
            new LocationCode(command.locationCode())
        );

        Location saved = locationRepository.save(location);

        evictLocationCache();
        
        return saved;
    }
    
    @Override
    public Location updateLocation(Long id, UpdateLocationCommand command) {
        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new LocationNotFoundException(id));

        location.update(command.name(), command.country(), command.city());

        Location saved = locationRepository.save(location);

        evictLocationCache();
        cachePort.evict(CACHE_KEY_PREFIX + id);
        
        return saved;
    }

    @Override
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException(id);
        }

        long referenceCount = transportationRepository.countByLocationId(id);
        if (referenceCount > 0) {
            throw new DomainException(
                String.format("Cannot delete location %d: %d transportation(s) reference this location. " +
                              "Delete or reassign the transportations first.", 
                              id, referenceCount));
        }
        
        locationRepository.deleteById(id);

        evictLocationCache();
        cachePort.evict(CACHE_KEY_PREFIX + id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Location getLocation(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Location cached = cachePort.get(cacheKey, Location.class).orElse(null);
        if (cached != null) {
            return cached;
        }

        Location location = locationRepository.findById(id)
            .orElseThrow(() -> new LocationNotFoundException(id));

        cachePort.set(cacheKey, location, cacheTtl);
        
        return location;
    }
    

    @Override
    @Transactional(readOnly = true)
    public Page<Location> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    private void evictLocationCache() {
        cachePort.evictByPattern("locations:*");
        cachePort.evictByPattern("routes:*");
        cachePort.evictByPattern("graph:*");
    }
}
