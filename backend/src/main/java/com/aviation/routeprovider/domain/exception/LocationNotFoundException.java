package com.aviation.routeprovider.domain.exception;

public class LocationNotFoundException extends DomainException {
    
    public LocationNotFoundException(Long id) {
        super("Location not found with id: " + id);
    }

}
