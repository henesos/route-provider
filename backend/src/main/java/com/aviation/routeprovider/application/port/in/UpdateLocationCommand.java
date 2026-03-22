package com.aviation.routeprovider.application.port.in;

public record UpdateLocationCommand(
    String name,
    String country,
    String city
) {
}
