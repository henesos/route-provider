package com.aviation.routeprovider.application.port.in;

public record CreateTransportationCommand(
    Long originLocationId,
    Long destinationLocationId,
    String transportationType,
    int[] operatingDays
) {
    public CreateTransportationCommand {
        if (originLocationId == null) {
            throw new IllegalArgumentException("Origin location ID is required");
        }
        if (destinationLocationId == null) {
            throw new IllegalArgumentException("Destination location ID is required");
        }
        if (transportationType == null || transportationType.isBlank()) {
            throw new IllegalArgumentException("Transportation type is required");
        }
        if (operatingDays == null || operatingDays.length == 0) {
            throw new IllegalArgumentException("Operating days are required");
        }
        if (originLocationId.equals(destinationLocationId)) {
            throw new IllegalArgumentException(
                "Origin and destination cannot be the same");
        }
    }
}
