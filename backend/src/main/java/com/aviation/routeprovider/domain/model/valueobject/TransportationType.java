package com.aviation.routeprovider.domain.model.valueobject;

public enum TransportationType {
    
    FLIGHT,
    BUS,
    SUBWAY,
    UBER;

    public boolean isFlight() {
        return this == FLIGHT;
    }

    public boolean isGroundTransportation() {
        return !isFlight();
    }
}
