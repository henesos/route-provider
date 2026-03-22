package com.aviation.routeprovider.api.rest.dto.response;

import java.util.List;

public class RouteResponse {

    private LocationSummary origin;
    private LocationSummary destination;
    private int transportationCount;
    private List<TransportationSegment> segments;

    public RouteResponse() {}

    public RouteResponse(LocationSummary origin, LocationSummary destination, 
                         int transportationCount, List<TransportationSegment> segments) {
        this.origin = origin;
        this.destination = destination;
        this.transportationCount = transportationCount;
        this.segments = segments;
    }

    // Getters
    public LocationSummary getOrigin() { return origin; }
    public LocationSummary getDestination() { return destination; }
    public int getTransportationCount() { return transportationCount; }
    public List<TransportationSegment> getSegments() { return segments; }

    // Setters
    public void setOrigin(LocationSummary origin) { this.origin = origin; }
    public void setDestination(LocationSummary destination) { this.destination = destination; }
    public void setTransportationCount(int transportationCount) { this.transportationCount = transportationCount; }
    public void setSegments(List<TransportationSegment> segments) { this.segments = segments; }

    public static class LocationSummary {
        private Long id;
        private String name;
        private String locationCode;
        private String city;
        private String country;

        public LocationSummary() {}

        public LocationSummary(Long id, String name, String locationCode, String city, String country) {
            this.id = id;
            this.name = name;
            this.locationCode = locationCode;
            this.city = city;
            this.country = country;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getLocationCode() { return locationCode; }
        public String getCity() { return city; }
        public String getCountry() { return country; }

        // Setters
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
        public void setCity(String city) { this.city = city; }
        public void setCountry(String country) { this.country = country; }
    }

    public static class TransportationSegment {
        private String type;
        private LocationSummary from;
        private LocationSummary to;
        private int[] operatingDays;

        public TransportationSegment() {}

        public TransportationSegment(String type, LocationSummary from, LocationSummary to, int[] operatingDays) {
            this.type = type;
            this.from = from;
            this.to = to;
            this.operatingDays = operatingDays;
        }

        // Getters
        public String getType() { return type; }
        public LocationSummary getFrom() { return from; }
        public LocationSummary getTo() { return to; }
        public int[] getOperatingDays() { return operatingDays; }

        // Setters
        public void setType(String type) { this.type = type; }
        public void setFrom(LocationSummary from) { this.from = from; }
        public void setTo(LocationSummary to) { this.to = to; }
        public void setOperatingDays(int[] operatingDays) { this.operatingDays = operatingDays; }
    }
}
