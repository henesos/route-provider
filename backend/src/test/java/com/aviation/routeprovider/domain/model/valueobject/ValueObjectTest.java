package com.aviation.routeprovider.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectTest {
    
    @Test
    @DisplayName("LocationCode - should create valid IATA code")
    void shouldCreateValidIataCode() {
        LocationCode code = new LocationCode("IST");
        
        assertEquals("IST", code.getValue());
        assertTrue(code.isIataCode());
        assertFalse(code.isCustomCode());
    }
    
    @Test
    @DisplayName("LocationCode - should create valid custom code")
    void shouldCreateValidCustomCode() {
        LocationCode code = new LocationCode("CCIST");
        
        assertEquals("CCIST", code.getValue());
        assertFalse(code.isIataCode());
        assertTrue(code.isCustomCode());
    }
    
    @Test
    @DisplayName("LocationCode - should convert to uppercase")
    void shouldConvertToUppercase() {
        LocationCode code = new LocationCode("ist");
        
        assertEquals("IST", code.getValue());
    }
    
    @Test
    @DisplayName("LocationCode - should reject null")
    void shouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> new LocationCode(null));
    }
    
    @Test
    @DisplayName("LocationCode - should reject empty string")
    void shouldRejectEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> new LocationCode(""));
    }
    
    @Test
    @DisplayName("LocationCode - should reject code shorter than 3 characters")
    void shouldRejectShortCode() {
        assertThrows(IllegalArgumentException.class, () -> new LocationCode("AB"));
    }
    
    @Test
    @DisplayName("LocationCode - should reject code longer than 10 characters")
    void shouldRejectLongCode() {
        assertThrows(IllegalArgumentException.class, () -> new LocationCode("ABCDEFGHIJK"));
    }
    
    @Test
    @DisplayName("LocationCode - should reject non-alphanumeric characters")
    void shouldRejectNonAlphanumeric() {
        assertThrows(IllegalArgumentException.class, () -> new LocationCode("IS!"));
    }
    
    @Test
    @DisplayName("OperatingDays - should create from array")
    void shouldCreateOperatingDaysFromArray() {
        OperatingDays days = new OperatingDays(1, 3, 5);
        
        assertTrue(days.operatesOn(DayOfWeek.MONDAY));
        assertFalse(days.operatesOn(DayOfWeek.TUESDAY));
        assertTrue(days.operatesOn(DayOfWeek.WEDNESDAY));
    }
    
    @Test
    @DisplayName("OperatingDays - should create daily operation")
    void shouldCreateDailyOperation() {
        OperatingDays days = OperatingDays.daily();
        
        assertTrue(days.isDaily());
        for (DayOfWeek day : DayOfWeek.values()) {
            assertTrue(days.operatesOn(day));
        }
    }
    
    @Test
    @DisplayName("OperatingDays - should create empty operation")
    void shouldCreateEmptyOperation() {
        OperatingDays days = OperatingDays.empty();
        
        assertTrue(days.isEmpty());
        for (DayOfWeek day : DayOfWeek.values()) {
            assertFalse(days.operatesOn(day));
        }
    }
    
    @Test
    @DisplayName("OperatingDays - should reject invalid day numbers")
    void shouldRejectInvalidDayNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new OperatingDays(0));
        assertThrows(IllegalArgumentException.class, () -> new OperatingDays(8));
    }
    
    @Test
    @DisplayName("TransportationType - should identify flight correctly")
    void shouldIdentifyFlightCorrectly() {
        assertTrue(TransportationType.FLIGHT.isFlight());
        assertFalse(TransportationType.FLIGHT.isGroundTransportation());
        
        assertFalse(TransportationType.BUS.isFlight());
        assertTrue(TransportationType.BUS.isGroundTransportation());
    }
    
    @Test
    @DisplayName("UserRole - should have correct permissions")
    void shouldHaveCorrectPermissions() {
        assertTrue(UserRole.ADMIN.canManageLocations());
        assertTrue(UserRole.ADMIN.canManageTransportations());
        assertTrue(UserRole.ADMIN.canSearchRoutes());
        
        assertFalse(UserRole.AGENCY.canManageLocations());
        assertFalse(UserRole.AGENCY.canManageTransportations());
        assertTrue(UserRole.AGENCY.canSearchRoutes());
    }
}
