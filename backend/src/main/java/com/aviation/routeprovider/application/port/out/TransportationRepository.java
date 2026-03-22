package com.aviation.routeprovider.application.port.out;

import com.aviation.routeprovider.domain.model.entity.Transportation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransportationRepository {

    Transportation save(Transportation transportation);

    Optional<Transportation> findById(Long id);

    Page<Transportation> findAll(Pageable pageable);

    void deleteById(Long id);

    boolean existsById(Long id);

    Map<Long, List<Transportation>> loadAdjacencyMap(DayOfWeek day);

    long countByLocationId(Long locationId);
}
