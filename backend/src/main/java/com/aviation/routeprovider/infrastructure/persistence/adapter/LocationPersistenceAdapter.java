package com.aviation.routeprovider.infrastructure.persistence.adapter;

import com.aviation.routeprovider.application.port.out.LocationRepository;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.mapper.LocationPersistenceMapper;
import com.aviation.routeprovider.infrastructure.persistence.repository.LocationJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class LocationPersistenceAdapter implements LocationRepository {
    
    private final LocationJpaRepository jpaRepository;
    private final LocationPersistenceMapper mapper;
    
    public LocationPersistenceAdapter(LocationJpaRepository jpaRepository,
                                       LocationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Location save(Location location) {
        LocationJpaEntity jpaEntity = mapper.toJpaEntity(location);
        LocationJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findByLocationCode(String locationCode) {
        return jpaRepository.findByLocationCode(locationCode)
            .map(mapper::toDomain);
    }
    
    // FIX [ISSUE 1]: Removed findAll() returning List<Location> - dead code, only paginated version used
    
    @Override
    @Transactional(readOnly = true)
    public Page<Location> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
            .map(mapper::toDomain);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByLocationCode(String locationCode) {
        return jpaRepository.existsByLocationCode(locationCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
