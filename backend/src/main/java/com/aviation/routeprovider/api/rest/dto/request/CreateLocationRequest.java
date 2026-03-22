package com.aviation.routeprovider.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class CreateLocationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Location code is required")
    @Size(min = 3, max = 10, message = "Location code must be between 3 and 10 characters")
    private String locationCode;

    public CreateLocationRequest() {}

    public CreateLocationRequest(String name, String country, String city, String locationCode) {
        this.name = name;
        this.country = country;
        this.city = city;
        this.locationCode = locationCode;
    }

    // Getters
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getCity() { return city; }
    public String getLocationCode() { return locationCode; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }
    public void setCity(String city) { this.city = city; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
}
