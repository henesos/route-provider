package com.aviation.routeprovider.infrastructure.persistence.repository;

import com.aviation.routeprovider.infrastructure.persistence.entity.TransportationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportationJpaRepository extends JpaRepository<TransportationJpaEntity, Long> {

    List<TransportationJpaEntity> findByOriginLocationId(Long originLocationId);

    @EntityGraph(value = "Transportation.withLocations", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT t FROM TransportationJpaEntity t")
    Page<TransportationJpaEntity> findAllWithLocations(Pageable pageable);

    @Query(value = "SELECT t.id FROM transportations t " +
           "WHERE :dayNumber = ANY(t.operating_days)", 
           nativeQuery = true)
    List<Long> findIdsByOperatingDay(@Param("dayNumber") int dayNumber);

    @Query("SELECT t FROM TransportationJpaEntity t " +
           "JOIN FETCH t.originLocation " +
           "JOIN FETCH t.destinationLocation " +
           "WHERE t.id IN :ids")
    List<TransportationJpaEntity> findByIdsWithLocations(@Param("ids") List<Long> ids);

    @EntityGraph(value = "Transportation.withLocations", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT t FROM TransportationJpaEntity t")
    List<TransportationJpaEntity> findAllWithLocations();

    long countByOriginLocationId(Long locationId);

    long countByDestinationLocationId(Long locationId);
}
