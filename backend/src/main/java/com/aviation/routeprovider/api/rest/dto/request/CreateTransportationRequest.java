package com.aviation.routeprovider.api.rest.dto.request;

import com.aviation.routeprovider.api.rest.dto.validation.ValidOperatingDays;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class CreateTransportationRequest {

    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    @NotEmpty(message = "Transportation type is required")
    private String transportationType;

    @NotEmpty(message = "Operating days are required")
    @Size(min = 1, max = 7, message = "Operating days must have between 1 and 7 days")
    @ValidOperatingDays
    private int[] operatingDays;

    public CreateTransportationRequest() {}

    public CreateTransportationRequest(Long originLocationId, Long destinationLocationId, 
                                       String transportationType, int[] operatingDays) {
        this.originLocationId = originLocationId;
        this.destinationLocationId = destinationLocationId;
        this.transportationType = transportationType;
        this.operatingDays = operatingDays;
    }

    // Getters
    public Long getOriginLocationId() { return originLocationId; }
    public Long getDestinationLocationId() { return destinationLocationId; }
    public String getTransportationType() { return transportationType; }
    public int[] getOperatingDays() { return operatingDays; }

    // Setters
    public void setOriginLocationId(Long originLocationId) { this.originLocationId = originLocationId; }
    public void setDestinationLocationId(Long destinationLocationId) { this.destinationLocationId = destinationLocationId; }
    public void setTransportationType(String transportationType) { this.transportationType = transportationType; }
    public void setOperatingDays(int[] operatingDays) { this.operatingDays = operatingDays; }
}
