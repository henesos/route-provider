package com.aviation.routeprovider.application.port.in;

import com.aviation.routeprovider.domain.model.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface LocationUseCase {
    

    Location createLocation(CreateLocationCommand command);

    Location updateLocation(Long id, UpdateLocationCommand command);

    void deleteLocation(Long id);

    Location getLocation(Long id);

    Page<Location> getAllLocations(Pageable pageable);

}
