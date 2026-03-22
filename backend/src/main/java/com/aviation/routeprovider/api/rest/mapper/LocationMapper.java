package com.aviation.routeprovider.api.rest.mapper;

import com.aviation.routeprovider.api.rest.dto.request.CreateLocationRequest;
import com.aviation.routeprovider.api.rest.dto.request.UpdateLocationRequest;
import com.aviation.routeprovider.api.rest.dto.response.LocationResponse;
import com.aviation.routeprovider.application.port.in.CreateLocationCommand;
import com.aviation.routeprovider.application.port.in.UpdateLocationCommand;
import com.aviation.routeprovider.domain.model.entity.Location;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationMapper {
    
    public CreateLocationCommand toCreateCommand(CreateLocationRequest request) {
        return new CreateLocationCommand(
            request.getName(),
            request.getCountry(),
            request.getCity(),
            request.getLocationCode()
        );
    }
    
    public UpdateLocationCommand toUpdateCommand(UpdateLocationRequest request) {
        return new UpdateLocationCommand(
            request.getName(),
            request.getCountry(),
            request.getCity()
        );
    }
    
    public LocationResponse toResponse(Location location) {
        return new LocationResponse(
            location.getId(),
            location.getName(),
            location.getCountry(),
            location.getCity(),
            location.getLocationCode().getValue(),
            location.isAirport()
        );
    }
    
    public List<LocationResponse> toResponseList(List<Location> locations) {
        return locations.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
