package com.aviation.routeprovider.api.rest.dto.validation;

import com.aviation.routeprovider.api.rest.dto.request.UpdateLocationRequest;
import com.aviation.routeprovider.api.rest.dto.request.UpdateTransportationRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for @AtLeastOneField validation.
 */
class AtLeastOneFieldValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===================== UpdateTransportationRequest Tests =====================

    @Test
    @DisplayName("Should pass when transportationType is provided")
    void shouldPassWhenTransportationTypeProvided() {
        UpdateTransportationRequest request = new UpdateTransportationRequest("FLIGHT", null);
        Set<ConstraintViolation<UpdateTransportationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when transportationType is provided");
    }

    @Test
    @DisplayName("Should pass when operatingDays is provided")
    void shouldPassWhenOperatingDaysProvided() {
        UpdateTransportationRequest request = new UpdateTransportationRequest(null, new int[]{1, 2, 3});
        Set<ConstraintViolation<UpdateTransportationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when operatingDays is provided");
    }

    @Test
    @DisplayName("Should pass when both fields are provided")
    void shouldPassWhenBothFieldsProvided() {
        UpdateTransportationRequest request = new UpdateTransportationRequest("BUS", new int[]{1, 2, 3, 4, 5});
        Set<ConstraintViolation<UpdateTransportationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when both fields are provided");
    }

    @Test
    @DisplayName("Should fail when no fields are provided")
    void shouldFailWhenNoFieldsProvided() {
        UpdateTransportationRequest request = new UpdateTransportationRequest(null, null);
        Set<ConstraintViolation<UpdateTransportationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations when no fields are provided");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("At least one field"));
    }

    @Test
    @DisplayName("Should fail when operatingDays is empty array")
    void shouldFailWhenOperatingDaysIsEmpty() {
        UpdateTransportationRequest request = new UpdateTransportationRequest(null, new int[]{});
        Set<ConstraintViolation<UpdateTransportationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations when operatingDays is empty");
    }

    @Test
    @DisplayName("Should pass when transportationType is empty string but operatingDays provided")
    void shouldPassWhenTransportationTypeEmptyButOperatingDaysProvided() {
        // Empty string is still considered "provided" for the validator
        // Additional validation (like @NotBlank) would catch this
        UpdateTransportationRequest request = new UpdateTransportationRequest("", new int[]{1, 2, 3});
        Set<ConstraintViolation<UpdateTransportationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should pass because operatingDays is provided");
    }

    // ===================== UpdateLocationRequest Tests =====================

    @Test
    @DisplayName("Should pass when name is provided for location update")
    void shouldPassWhenNameProvidedForLocation() {
        UpdateLocationRequest request = new UpdateLocationRequest("New Name", null, null);
        Set<ConstraintViolation<UpdateLocationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when name is provided");
    }

    @Test
    @DisplayName("Should pass when country is provided for location update")
    void shouldPassWhenCountryProvidedForLocation() {
        UpdateLocationRequest request = new UpdateLocationRequest(null, "New Country", null);
        Set<ConstraintViolation<UpdateLocationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when country is provided");
    }

    @Test
    @DisplayName("Should pass when city is provided for location update")
    void shouldPassWhenCityProvidedForLocation() {
        UpdateLocationRequest request = new UpdateLocationRequest(null, null, "New City");
        Set<ConstraintViolation<UpdateLocationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when city is provided");
    }

    @Test
    @DisplayName("Should fail when no fields provided for location update")
    void shouldFailWhenNoFieldsProvidedForLocation() {
        UpdateLocationRequest request = new UpdateLocationRequest(null, null, null);
        Set<ConstraintViolation<UpdateLocationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations when no fields are provided");
        assertEquals(1, violations.size());
    }

    @Test
    @DisplayName("Should pass when all fields provided for location update")
    void shouldPassWhenAllFieldsProvidedForLocation() {
        UpdateLocationRequest request = new UpdateLocationRequest("Name", "Country", "City");
        Set<ConstraintViolation<UpdateLocationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should have no violations when all fields are provided");
    }
}
