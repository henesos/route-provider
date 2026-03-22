package com.aviation.routeprovider.api.rest.mapper;

import com.aviation.routeprovider.api.rest.dto.request.RouteSearchRequest;
import com.aviation.routeprovider.api.rest.dto.response.RouteResponse;
import com.aviation.routeprovider.application.port.in.RouteSearchQuery;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.Route;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RouteMapper {

    public RouteSearchQuery toQuery(RouteSearchRequest request) {
        return new RouteSearchQuery(
            request.getOriginLocationId(),
            request.getDestinationLocationId(),
            request.getTravelDate()
        );
    }

    public RouteResponse toResponse(Route route) {
        return new RouteResponse(
            toLocationSummary(route.getOrigin()),
            toLocationSummary(route.getDestination()),
            route.getTransportationCount(),
            route.getTransportations().stream()
                .map(this::toSegment)
                .collect(Collectors.toList())
        );
    }

    public List<RouteResponse> toResponseList(List<Route> routes) {
        return routes.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    private RouteResponse.LocationSummary toLocationSummary(Location location) {
        return new RouteResponse.LocationSummary(
            location.getId(),
            location.getName(),
            location.getLocationCode().getValue(),
            location.getCity(),
            location.getCountry()
        );
    }
    
    private RouteResponse.TransportationSegment toSegment(Transportation transportation) {
        return new RouteResponse.TransportationSegment(
            transportation.getTransportationType().name(),
            toLocationSummary(transportation.getOriginLocation()),
            toLocationSummary(transportation.getDestinationLocation()),
            transportation.getOperatingDays().toArray()
        );
    }
}
