package com.aviation.routeprovider.domain.model.valueobject;

import com.aviation.routeprovider.domain.exception.InvalidRouteException;
import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Route {
    
    private final List<Transportation> transportations;
    private final Location origin;
    private final Location destination;

    public Route(List<Transportation> transportations) {
        validate(transportations);
        this.transportations = List.copyOf(transportations);
        this.origin = transportations.get(0).getOriginLocation();
        this.destination = transportations.get(transportations.size() - 1)
            .getDestinationLocation();
    }

    public List<Transportation> getTransportations() {
        return transportations;
    }

    public Location getOrigin() {
        return origin;
    }

    public Location getDestination() {
        return destination;
    }

    public int getTransportationCount() {
        return transportations.size();
    }

    public Transportation getFlight() {
        return transportations.stream()
            .filter(Transportation::isFlight)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No flight found in route - this should never happen"));
    }

    public List<Transportation> getPreFlightTransfers() {
        List<Transportation> transfers = new ArrayList<>();
        for (Transportation t : transportations) {
            if (t.isFlight()) {
                break;
            }
            transfers.add(t);
        }
        return Collections.unmodifiableList(transfers);
    }

    public List<Transportation> getPostFlightTransfers() {
        List<Transportation> transfers = new ArrayList<>();
        boolean foundFlight = false;
        for (Transportation t : transportations) {
            if (t.isFlight()) {
                foundFlight = true;
            } else if (foundFlight) {
                transfers.add(t);
            }
        }
        return Collections.unmodifiableList(transfers);
    }

    public boolean hasPreFlightTransfer() {
        return !getPreFlightTransfers().isEmpty();
    }

    public boolean hasPostFlightTransfer() {
        return !getPostFlightTransfers().isEmpty();
    }
    
    // Validation
    
    private void validate(List<Transportation> transportations) {
        if (transportations == null || transportations.isEmpty()) {
            throw new InvalidRouteException("Route cannot be empty");
        }
        
        validateSize(transportations);
        validateFlightCount(transportations);
        validateConnectivity(transportations);
        validateTransferOrder(transportations);
    }
    
    private void validateSize(List<Transportation> transportations) {
        if (transportations.size() > RouteConstraints.MAX_TRANSPORTATIONS) {
            throw InvalidRouteException.exceedsMaxTransportations(RouteConstraints.MAX_TRANSPORTATIONS);
        }
    }
    
    private void validateFlightCount(List<Transportation> transportations) {
        long flightCount = transportations.stream()
            .filter(Transportation::isFlight)
            .count();
        
        if (flightCount == 0) {
            throw InvalidRouteException.noFlight();
        }
        if (flightCount > 1) {
            throw InvalidRouteException.multipleFlights();
        }
    }
    
    private void validateConnectivity(List<Transportation> transportations) {
        for (int i = 0; i < transportations.size() - 1; i++) {
            Transportation current = transportations.get(i);
            Transportation next = transportations.get(i + 1);
            
            if (!current.connectsTo(next)) {
                throw InvalidRouteException.notConnected(i);
            }
        }
    }
    
    private void validateTransferOrder(List<Transportation> transportations) {
        boolean foundFlight = false;
        int preFlightCount = 0;
        int postFlightCount = 0;
        
        for (Transportation t : transportations) {
            if (t.isFlight()) {
                foundFlight = true;
            } else if (foundFlight) {
                postFlightCount++;
                if (postFlightCount > RouteConstraints.MAX_POST_FLIGHT_TRANSFERS) {
                    throw InvalidRouteException.multiplePostFlightTransfers();
                }
            } else {
                preFlightCount++;
                if (preFlightCount > RouteConstraints.MAX_PRE_FLIGHT_TRANSFERS) {
                    throw InvalidRouteException.multiplePreFlightTransfers();
                }
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(transportations, route.transportations);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transportations);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Route{");
        for (int i = 0; i < transportations.size(); i++) {
            if (i > 0) sb.append(" -> ");
            Transportation t = transportations.get(i);
            sb.append(t.getTransportationType())
              .append("(")
              .append(t.getOriginLocation().getLocationCode())
              .append("-")
              .append(t.getDestinationLocation().getLocationCode())
              .append(")");
        }
        sb.append("}");
        return sb.toString();
    }
}
