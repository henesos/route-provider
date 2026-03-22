package com.aviation.routeprovider.api.rest.dto.response;

public class TransportationResponse {

    private Long id;
    private LocationSummary originLocation;
    private LocationSummary destinationLocation;
    private String transportationType;
    private int[] operatingDays;

    public TransportationResponse() {}

    public TransportationResponse(Long id, LocationSummary originLocation, 
                                  LocationSummary destinationLocation, String transportationType, 
                                  int[] operatingDays) {
        this.id = id;
        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
        this.transportationType = transportationType;
        this.operatingDays = operatingDays;
    }

    // Getters
    public Long getId() { return id; }
    public LocationSummary getOriginLocation() { return originLocation; }
    public LocationSummary getDestinationLocation() { return destinationLocation; }
    public String getTransportationType() { return transportationType; }
    public int[] getOperatingDays() { return operatingDays; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setOriginLocation(LocationSummary originLocation) { this.originLocation = originLocation; }
    public void setDestinationLocation(LocationSummary destinationLocation) { this.destinationLocation = destinationLocation; }
    public void setTransportationType(String transportationType) { this.transportationType = transportationType; }
    public void setOperatingDays(int[] operatingDays) { this.operatingDays = operatingDays; }

    public static class LocationSummary {
        private Long id;
        private String name;
        private String locationCode;

        public LocationSummary() {}

        public LocationSummary(Long id, String name, String locationCode) {
            this.id = id;
            this.name = name;
            this.locationCode = locationCode;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getLocationCode() { return locationCode; }

        // Setters
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    }
}
