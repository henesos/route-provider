package com.aviation.routeprovider.domain.model.valueobject;

public final class RouteConstraints {
    
    private RouteConstraints() {
    }

    public static final int MAX_TRANSPORTATIONS = 3;

    public static final int MIN_TRANSPORTATIONS = 1;

    public static final int MAX_PRE_FLIGHT_TRANSFERS = 1;

    public static final int MAX_POST_FLIGHT_TRANSFERS = 1;

    public static final int REQUIRED_FLIGHT_COUNT = 1;

    public static boolean isValidTransportationCount(int count) {
        return count >= MIN_TRANSPORTATIONS && count <= MAX_TRANSPORTATIONS;
    }

    public static boolean isValidPreFlightCount(int count) {
        return count >= 0 && count <= MAX_PRE_FLIGHT_TRANSFERS;
    }

    public static boolean isValidPostFlightCount(int count) {
        return count >= 0 && count <= MAX_POST_FLIGHT_TRANSFERS;
    }
}
