package com.aviation.routeprovider.domain.exception;

public class InvalidRouteException extends DomainException {
    
    public InvalidRouteException(String message) {
        super(message);
    }

    public static InvalidRouteException noFlight() {
        return new InvalidRouteException("Route must contain exactly one flight");
    }

    public static InvalidRouteException multipleFlights() {
        return new InvalidRouteException("Route cannot contain more than one flight");
    }

    public static InvalidRouteException exceedsMaxTransportations(int max) {
        return new InvalidRouteException(
            "Route cannot have more than " + max + " transportations");
    }

    public static InvalidRouteException notConnected(int position) {
        return new InvalidRouteException(
            "Transportations are not connected at position " + position);
    }

    public static InvalidRouteException multiplePreFlightTransfers() {
        return new InvalidRouteException(
            "Route cannot have more than one pre-flight transfer");
    }

    public static InvalidRouteException multiplePostFlightTransfers() {
        return new InvalidRouteException(
            "Route cannot have more than one post-flight transfer");
    }
}
