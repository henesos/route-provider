package com.aviation.routeprovider.api.rest.dto.request;

import com.aviation.routeprovider.api.rest.dto.validation.AtLeastOneField;
import com.aviation.routeprovider.api.rest.dto.validation.ValidOperatingDays;
import jakarta.validation.constraints.Size;

@AtLeastOneField(message = "At least one field (transportationType or operatingDays) must be provided for update")
public class UpdateTransportationRequest {

    private String transportationType;

    @Size(min = 1, max = 7, message = "Operating days must have between 1 and 7 days")
    @ValidOperatingDays
    private int[] operatingDays;

    public UpdateTransportationRequest() {}

    public UpdateTransportationRequest(String transportationType, int[] operatingDays) {
        this.transportationType = transportationType;
        this.operatingDays = operatingDays;
    }

    // Getters
    public String getTransportationType() { return transportationType; }
    public int[] getOperatingDays() { return operatingDays; }

    // Setters
    public void setTransportationType(String transportationType) { this.transportationType = transportationType; }
    public void setOperatingDays(int[] operatingDays) { this.operatingDays = operatingDays; }
}
