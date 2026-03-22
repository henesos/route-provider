package com.aviation.routeprovider.domain.exception;

public class TransportationNotFoundException extends DomainException {
    
    public TransportationNotFoundException(Long id) {
        super("Transportation not found with id: " + id);
    }
}
