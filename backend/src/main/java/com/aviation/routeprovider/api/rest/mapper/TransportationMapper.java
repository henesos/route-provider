package com.aviation.routeprovider.api.rest.mapper;

import com.aviation.routeprovider.api.rest.dto.request.CreateTransportationRequest;
import com.aviation.routeprovider.api.rest.dto.request.UpdateTransportationRequest;
import com.aviation.routeprovider.api.rest.dto.response.TransportationResponse;
import com.aviation.routeprovider.application.port.in.CreateTransportationCommand;
import com.aviation.routeprovider.application.port.in.UpdateTransportationCommand;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransportationMapper {
    
    public CreateTransportationCommand toCreateCommand(CreateTransportationRequest request) {
        return new CreateTransportationCommand(
            request.getOriginLocationId(),
            request.getDestinationLocationId(),
            request.getTransportationType(),
            request.getOperatingDays()
        );
    }
    
    public UpdateTransportationCommand toUpdateCommand(UpdateTransportationRequest request) {
        return new UpdateTransportationCommand(
            request.getTransportationType(),
            request.getOperatingDays()
        );
    }
    
    public TransportationResponse toResponse(Transportation transportation) {
        return new TransportationResponse(
            transportation.getId(),
            toLocationSummary(transportation.getOriginLocation()),
            toLocationSummary(transportation.getDestinationLocation()),
            transportation.getTransportationType().name(),
            transportation.getOperatingDays().toArray()
        );
    }
    
    public List<TransportationResponse> toResponseList(List<Transportation> transportations) {
        return transportations.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    private TransportationResponse.LocationSummary toLocationSummary(
            com.aviation.routeprovider.domain.model.entity.Location location) {
        return new TransportationResponse.LocationSummary(
            location.getId(),
            location.getName(),
            location.getLocationCode().getValue()
        );
    }
}
