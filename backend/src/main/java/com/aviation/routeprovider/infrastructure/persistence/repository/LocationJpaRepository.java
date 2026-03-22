package com.aviation.routeprovider.infrastructure.persistence.repository;

import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationJpaRepository extends JpaRepository<LocationJpaEntity, Long> {

    Optional<LocationJpaEntity> findByLocationCode(String locationCode);

    boolean existsByLocationCode(String locationCode);

    java.util.List<LocationJpaEntity> findByCity(String city);

    java.util.List<LocationJpaEntity> findByCountry(String country);
}
