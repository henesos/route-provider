package com.aviation.routeprovider.api.rest.dto.request;

import com.aviation.routeprovider.api.rest.dto.validation.AtLeastOneField;
import jakarta.validation.constraints.Size;

@AtLeastOneField(message = "At least one field (name, country, or city) must be provided for update")
public class UpdateLocationRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    public UpdateLocationRequest() {}

    public UpdateLocationRequest(String name, String country, String city) {
        this.name = name;
        this.country = country;
        this.city = city;
    }

    // Getters
    public String getName() { return name; }
    public String getCountry() { return country; }
    public String getCity() { return city; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }
    public void setCity(String city) { this.city = city; }
}
