package com.aviation.routeprovider.domain.model.entity;

import com.aviation.routeprovider.domain.exception.DomainException;
import com.aviation.routeprovider.domain.model.valueobject.LocationCode;

import java.util.Objects;

public class Location {

    private Long id;
    private String name;
    private String country;
    private String city;
    private LocationCode locationCode;

    protected Location() {}

    private Location(Long id, String name, String country, String city, LocationCode locationCode) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.city = city;
        this.locationCode = locationCode;
    }

    public static Location create(String name, String country,
                                   String city, LocationCode locationCode) {
        validateRequired(name, country, city, locationCode);
        return new Location(null, name.trim(), country.trim(),
                           city.trim(), locationCode);
    }

    public static Location reconstruct(Long id, String name, String country,
                                        String city, LocationCode locationCode) {
        if (id == null) {
            throw new DomainException("ID is required for reconstruction");
        }
        validateRequired(name, country, city, locationCode);
        return new Location(id, name, country, city, locationCode);
    }

    public void update(String name, String country, String city) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (country != null && !country.isBlank()) {
            this.country = country.trim();
        }
        if (city != null && !city.isBlank()) {
            this.city = city.trim();
        }
    }

    public boolean isAirport() {
        return locationCode != null && locationCode.isIataCode();
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public LocationCode getLocationCode() {
        return locationCode;
    }

    private static void validateRequired(String name, String country,
                                          String city, LocationCode locationCode) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Location name is required");
        }
        if (country == null || country.isBlank()) {
            throw new DomainException("Country is required");
        }
        if (city == null || city.isBlank()) {
            throw new DomainException("City is required");
        }
        if (locationCode == null) {
            throw new DomainException("Location code is required");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return id != null && Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return String.format("Location{id=%d, name='%s', code='%s'}",
            id, name, locationCode);
    }
}
