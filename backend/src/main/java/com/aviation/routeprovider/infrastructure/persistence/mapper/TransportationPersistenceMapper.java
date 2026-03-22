package com.aviation.routeprovider.infrastructure.persistence.mapper;

import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.infrastructure.persistence.entity.TransportationJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class TransportationPersistenceMapper {
    
    private final LocationPersistenceMapper locationMapper;
    
    public TransportationPersistenceMapper(LocationPersistenceMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    public Transportation toDomain(TransportationJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        
        return Transportation.reconstruct(
            jpaEntity.getId(),
            locationMapper.toDomain(jpaEntity.getOriginLocation()),
            locationMapper.toDomain(jpaEntity.getDestinationLocation()),
            jpaEntity.getTransportationType(),
            new OperatingDays(jpaEntity.getOperatingDays())
        );
    }

    public TransportationJpaEntity toJpaEntity(Transportation transportation) {
        if (transportation == null) {
            return null;
        }
        
        TransportationJpaEntity jpaEntity = new TransportationJpaEntity();
        jpaEntity.setId(transportation.getId());
        jpaEntity.setTransportationType(transportation.getTransportationType());
        jpaEntity.setOperatingDays(transportation.getOperatingDays().toArray());
        
        return jpaEntity;
    }

    public void updateJpaEntity(Transportation transportation, 
                                 TransportationJpaEntity jpaEntity) {
        if (transportation == null || jpaEntity == null) {
            return;
        }
        
        jpaEntity.setTransportationType(transportation.getTransportationType());
        jpaEntity.setOperatingDays(transportation.getOperatingDays().toArray());
    }
}
