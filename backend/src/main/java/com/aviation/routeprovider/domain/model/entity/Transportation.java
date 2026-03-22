package com.aviation.routeprovider.domain.model.entity;

import com.aviation.routeprovider.domain.exception.DomainException;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.domain.model.valueobject.TransportationType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class Transportation {

    private Long id;
    private Location originLocation;
    private Location destinationLocation;
    private TransportationType transportationType;
    private OperatingDays operatingDays;

    protected Transportation() {}

    private Transportation(Long id, Location originLocation, Location destinationLocation,
                          TransportationType transportationType, OperatingDays operatingDays) {
        this.id = id;
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
        this.transportationType = transportationType;
        this.operatingDays = operatingDays;
    }

    public static Transportation create(Location originLocation, Location destinationLocation,
                                         TransportationType transportationType,
                                         OperatingDays operatingDays) {
        validate(originLocation, destinationLocation, transportationType, operatingDays);
        return new Transportation(null, originLocation, destinationLocation,
                                 transportationType, operatingDays);
    }

    public static Transportation reconstruct(Long id, Location originLocation,
                                              Location destinationLocation,
                                              TransportationType transportationType,
                                              OperatingDays operatingDays) {
        if (id == null) {
            throw new DomainException("ID is required for reconstruction");
        }
        validate(originLocation, destinationLocation, transportationType, operatingDays);
        return new Transportation(id, originLocation, destinationLocation,
                                 transportationType, operatingDays);
    }

    public void update(TransportationType transportationType, OperatingDays operatingDays) {
        if (transportationType != null) {
            this.transportationType = transportationType;
        }
        if (operatingDays != null) {
            this.operatingDays = operatingDays;
        }
    }

    public boolean operatesOn(DayOfWeek day) {
        return operatingDays != null && operatingDays.operatesOn(day);
    }

    public boolean isFlight() {
        return transportationType != null && transportationType.isFlight();
    }

    public boolean isGroundTransportation() {
        return transportationType != null && transportationType.isGroundTransportation();
    }

    public boolean connectsTo(Transportation next) {
        if (next == null || destinationLocation == null || next.originLocation == null) {
            return false;
        }
        return this.destinationLocation.equals(next.originLocation);
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Location getOriginLocation() {
        return originLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public TransportationType getTransportationType() {
        return transportationType;
    }

    public OperatingDays getOperatingDays() {
        return operatingDays;
    }

    // Validation

    private static void validate(Location originLocation, Location destinationLocation,
                                  TransportationType transportationType,
                                  OperatingDays operatingDays) {
        if (originLocation == null) {
            throw new DomainException("Origin location is required");
        }
        if (destinationLocation == null) {
            throw new DomainException("Destination location is required");
        }
        if (transportationType == null) {
            throw new DomainException("Transportation type is required");
        }
        if (operatingDays == null || operatingDays.isEmpty()) {
            throw new DomainException("Operating days are required and must not be empty");
        }
        if (originLocation.equals(destinationLocation)) {
            throw new DomainException(
                "Origin and destination locations cannot be the same");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transportation that = (Transportation) o;
        // ID-based equality only - standard entity pattern
        // If either ID is null, objects are not equal (transient entities)
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // FIX: Consistent with equals - use ID only
        // If ID is null, use a constant to satisfy hashCode contract
        // This ensures transient entities don't break HashSet/HashMap
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }

    public boolean hasSameBusinessIdentity(Transportation other) {
        if (other == null) return false;
        return Objects.equals(originLocation, other.originLocation) &&
               Objects.equals(destinationLocation, other.destinationLocation) &&
               transportationType == other.transportationType;
    }

    @Override
    public String toString() {
        return String.format("Transportation{id=%d, type=%s, from='%s', to='%s'}",
            id, transportationType,
            originLocation != null ? originLocation.getLocationCode() : null,
            destinationLocation != null ? destinationLocation.getLocationCode() : null);
    }
}
