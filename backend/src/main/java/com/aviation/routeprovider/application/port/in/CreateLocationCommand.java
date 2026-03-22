package com.aviation.routeprovider.application.port.in;

public record CreateLocationCommand(
    String name,
    String country,
    String city,
    String locationCode
) {
    public CreateLocationCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country is required");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (locationCode == null || locationCode.isBlank()) {
            throw new IllegalArgumentException("Location code is required");
        }
    }
}
