package com.aviation.routeprovider.domain.service;

import com.aviation.routeprovider.domain.model.entity.Location;
import com.aviation.routeprovider.domain.model.entity.Transportation;
import com.aviation.routeprovider.domain.model.valueobject.Route;
import com.aviation.routeprovider.domain.model.valueobject.RouteConstraints;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;

public class RouteEngine {
    

    public RouteEngine() {
    }

    public List<Route> findRoutes(
            Map<Long, List<Transportation>> adjacencyMap,
            Location origin,
            Location destination,
            LocalDate travelDate) {

        Objects.requireNonNull(adjacencyMap, "Adjacency map cannot be null");
        Objects.requireNonNull(origin, "Origin location cannot be null");
        Objects.requireNonNull(destination, "Destination location cannot be null");
        Objects.requireNonNull(travelDate, "Travel date cannot be null");
        
        if (origin.equals(destination)) {
            return Collections.emptyList();
        }
        
        List<Route> validRoutes = new ArrayList<>();
        DayOfWeek travelDay = travelDate.getDayOfWeek();

        Deque<RouteNode> queue = new ArrayDeque<>();
        queue.offer(RouteNode.start(origin));
        
        while (!queue.isEmpty()) {
            RouteNode current = queue.poll();

            if (current.hasReached(destination) && current.hasFlight()) {
                validRoutes.add(current.toRoute());
                continue;
            }

            if (current.getTransportationCount() >= RouteConstraints.MAX_TRANSPORTATIONS) {
                continue;
            }

            List<Transportation> outgoing = adjacencyMap.getOrDefault(
                current.getLocation().getId(), Collections.emptyList());
            
            for (Transportation transportation : outgoing) {
                if (!transportation.operatesOn(travelDay)) {
                    continue;
                }

                if (!canAddToPath(current, transportation)) {
                    continue;
                }

                RouteNode next = current.withTransportation(transportation);
                queue.offer(next);
            }
        }
        
        return validRoutes;
    }

    private boolean canAddToPath(RouteNode current, Transportation transportation) {
        if (transportation.isFlight() && current.hasFlight()) {
            return false;
        }

        if (current.hasFlight()) {
            if (current.getPostFlightCount() >= 1) {
                return false;
            }
        } else {
            if (!transportation.isFlight() && current.getPreFlightCount() >= 1) {
                return false;
            }
        }
        
        return true;
    }

    private static class RouteNode {
        private final Location location;
        private final List<Transportation> path;
        private final int transportationCount;
        private final boolean hasFlight;
        private final int preFlightCount;
        private final int postFlightCount;

        RouteNode(Location location, List<Transportation> path,
                  int transportationCount, boolean hasFlight,
                  int preFlightCount, int postFlightCount) {
            this.location = location;
            this.path = path;
            this.transportationCount = transportationCount;
            this.hasFlight = hasFlight;
            this.preFlightCount = preFlightCount;
            this.postFlightCount = postFlightCount;
        }

        static RouteNode start(Location origin) {
            return new RouteNode(origin, new ArrayList<>(), 0, false, 0, 0);
        }

        boolean hasReached(Location destination) {
            return location.equals(destination);
        }

        boolean hasFlight() {
            return hasFlight;
        }

        int getTransportationCount() {
            return transportationCount;
        }

        int getPreFlightCount() {
            return preFlightCount;
        }

        int getPostFlightCount() {
            return postFlightCount;
        }

        Location getLocation() {
            return location;
        }

        Route toRoute() {
            return new Route(path);
        }

        RouteNode withTransportation(Transportation transportation) {
            List<Transportation> newPath = new ArrayList<>(path);
            newPath.add(transportation);

            boolean newHasFlight = hasFlight || transportation.isFlight();
            int newPreFlightCount = preFlightCount;
            int newPostFlightCount = postFlightCount;

            if (!transportation.isFlight()) {
                if (hasFlight) {
                    newPostFlightCount = postFlightCount + 1;
                } else {
                    newPreFlightCount = preFlightCount + 1;
                }
            }

            return new RouteNode(
                transportation.getDestinationLocation(),
                newPath,
                transportationCount + 1,
                newHasFlight,
                newPreFlightCount,
                newPostFlightCount
            );
        }
    }
}
