package com.aviation.routeprovider.application.port.in;

public record UpdateTransportationCommand(
    String transportationType,
    int[] operatingDays
) {
}
