package com.aviation.routeprovider.application.port.out;

import com.aviation.routeprovider.domain.model.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LocationRepository {

    Location save(Location location);

    Optional<Location> findById(Long id);

    Optional<Location> findByLocationCode(String locationCode);

    Page<Location> findAll(Pageable pageable);

    void deleteById(Long id);

    boolean existsByLocationCode(String locationCode);

    boolean existsById(Long id);
}
