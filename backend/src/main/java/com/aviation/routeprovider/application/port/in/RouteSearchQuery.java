package com.aviation.routeprovider.application.port.in;

import java.time.LocalDate;

public record RouteSearchQuery(
    Long originLocationId,
    Long destinationLocationId,
    LocalDate travelDate
) {
    public RouteSearchQuery {
        if (originLocationId == null) {
            throw new IllegalArgumentException("Origin location ID is required");
        }
        if (destinationLocationId == null) {
            throw new IllegalArgumentException("Destination location ID is required");
        }
        if (travelDate == null) {
            throw new IllegalArgumentException("Travel date is required");
        }
        if (originLocationId.equals(destinationLocationId)) {
            throw new IllegalArgumentException(
                "Origin and destination cannot be the same");
        }
    }
}
