package com.aviation.routeprovider.api.rest.dto.request;

import com.aviation.routeprovider.api.rest.dto.validation.TodayOrFuture;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;


public class RouteSearchRequest {

    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    @NotNull(message = "Travel date is required")
    @TodayOrFuture
    private LocalDate travelDate;

    public RouteSearchRequest() {}

    public RouteSearchRequest(Long originLocationId, Long destinationLocationId, LocalDate travelDate) {
        this.originLocationId = originLocationId;
        this.destinationLocationId = destinationLocationId;
        this.travelDate = travelDate;
    }

    // Getters
    public Long getOriginLocationId() { return originLocationId; }
    public Long getDestinationLocationId() { return destinationLocationId; }
    public LocalDate getTravelDate() { return travelDate; }

    // Setters
    public void setOriginLocationId(Long originLocationId) { this.originLocationId = originLocationId; }
    public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
}
