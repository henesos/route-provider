package com.aviation.routeprovider.infrastructure.persistence.mapper;

import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.valueobject.LocationCode;
import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LocationPersistenceMapper {

    public Location toDomain(LocationJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return Location.reconstruct(
            jpaEntity.getId(),
            jpaEntity.getName(),
            jpaEntity.getCountry(),
            jpaEntity.getCity(),
            new LocationCode(jpaEntity.getLocationCode())
        );
    }

    public LocationJpaEntity toJpaEntity(Location location) {
        if (location == null) {
            return null;
        }
        
        LocationJpaEntity jpaEntity = new LocationJpaEntity();
        jpaEntity.setId(location.getId());
        jpaEntity.setName(location.getName());
        jpaEntity.setCountry(location.getCountry());
        jpaEntity.setCity(location.getCity());
        jpaEntity.setLocationCode(location.getLocationCode().getValue());
        
        return jpaEntity;
    }

    public void updateJpaEntity(Location location, LocationJpaEntity jpaEntity) {
        if (location == null || jpaEntity == null) {
            return;
        }
        
        jpaEntity.setName(location.getName());
        jpaEntity.setCountry(location.getCountry());
        jpaEntity.setCity(location.getCity());
        jpaEntity.setLocationCode(location.getLocationCode().getValue());
    }
}
