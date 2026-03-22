package com.aviation.routeprovider.application.service;

import com.aviation.routeprovider.application.config.CacheConfiguration;
import com.aviation.routeprovider.application.port.in.CreateTransportationCommand;
import com.aviation.routeprovider.application.port.in.TransportationUseCase;
import com.aviation.routeprovider.application.port.in.UpdateTransportationCommand;
import com.aviation.routeprovider.application.port.out.CachePort;
import com.aviation.routeprovider.application.port.out.LocationRepository;
import com.aviation.routeprovider.application.port.out.TransportationRepository;
import com.aviation.routeprovider.domain.exception.DomainException;
import com.aviation.routeprovider.domain.exception.LocationNotFoundException;
import com.aviation.routeprovider.domain.exception.TransportationNotFoundException;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
public class TransportationService implements TransportationUseCase {

    private static final String CACHE_KEY_PREFIX = "transportations:";

    private final TransportationRepository transportationRepository;
    private final LocationRepository locationRepository;
    private final CachePort cachePort;
    private final Duration cacheTtl;

    public TransportationService(
            TransportationRepository transportationRepository,
            LocationRepository locationRepository,
            CachePort cachePort,
            CacheConfiguration cacheConfiguration) {
        this.transportationRepository = transportationRepository;
        this.locationRepository = locationRepository;
        this.cachePort = cachePort;
        this.cacheTtl = cacheConfiguration.getTransportationCacheTtl();
    }
    
    @Override
    public Transportation createTransportation(CreateTransportationCommand command) {
        Location origin = locationRepository.findById(command.originLocationId())
            .orElseThrow(() -> new LocationNotFoundException(command.originLocationId()));
        
        Location destination = locationRepository.findById(command.destinationLocationId())
            .orElseThrow(() -> new LocationNotFoundException(command.destinationLocationId()));

        TransportationType type;
        try {
            type = TransportationType.valueOf(command.transportationType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException(
                "Invalid transportation type: " + command.transportationType());
        }

        Transportation transportation = Transportation.create(
            origin, destination, type, new OperatingDays(command.operatingDays())
        );

        Transportation saved = transportationRepository.save(transportation);

        evictTransportationCache();
        
        return saved;
    }
    
    @Override
    public Transportation updateTransportation(Long id, UpdateTransportationCommand command) {
        Transportation transportation = transportationRepository.findById(id)
            .orElseThrow(() -> new TransportationNotFoundException(id));

        TransportationType type = null;
        if (command.transportationType() != null) {
            try {
                type = TransportationType.valueOf(command.transportationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new DomainException(
                    "Invalid transportation type: " + command.transportationType());
            }
        }
        
        OperatingDays operatingDays = null;
        if (command.operatingDays() != null) {
            operatingDays = new OperatingDays(command.operatingDays());
        }

        transportation.update(type, operatingDays);

        Transportation saved = transportationRepository.save(transportation);

        evictTransportationCache();
        cachePort.evict(CACHE_KEY_PREFIX + id);
        
        return saved;
    }
    
    @Override
    public void deleteTransportation(Long id) {
        if (!transportationRepository.existsById(id)) {
            throw new TransportationNotFoundException(id);
        }
        
        transportationRepository.deleteById(id);

        evictTransportationCache();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Transportation getTransportation(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Transportation cached = cachePort.get(cacheKey, Transportation.class).orElse(null);
        if (cached != null) {
            return cached;
        }

        Transportation transportation = transportationRepository.findById(id)
            .orElseThrow(() -> new TransportationNotFoundException(id));

        cachePort.set(cacheKey, transportation, cacheTtl);
        
        return transportation;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transportation> getAllTransportations(Pageable pageable) {
        return transportationRepository.findAll(pageable);
    }

    private void evictTransportationCache() {
        cachePort.evictByPattern("transportations:*");
        cachePort.evictByPattern("routes:*");
    }
}
