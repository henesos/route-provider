package com.aviation.routeprovider.domain.model.entity;

import com.aviation.routeprovider.domain.model.valueobject.LocationCode;
import com.aviation.routeprovider.domain.model.valueobject.OperatingDays;
import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class TransportationEqualityTest {

    private Location istanbul;
    private Location london;
    private Location paris;

    @BeforeEach
    void setUp() {
        istanbul = Location.reconstruct(1L, "Istanbul Airport", "Turkey", "Istanbul", new LocationCode("IST"));
        london = Location.reconstruct(2L, "London Heathrow", "UK", "London", new LocationCode("LHR"));
        paris = Location.reconstruct(3L, "Paris CDG", "France", "Paris", new LocationCode("CDG"));
    }

    @Nested
    @DisplayName("ID-based Equality")
    class IdBasedEqualityTests {

        @Test
        @DisplayName("Entities with same ID should be equal")
        void sameId_shouldBeEqual() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());

            assertEquals(t1, t2);
            assertEquals(t1.hashCode(), t2.hashCode());
        }

        @Test
        @DisplayName("Entities with different ID should not be equal")
        void differentId_shouldNotBeEqual() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(200L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());

            assertNotEquals(t1, t2);
        }

        @Test
        @DisplayName("Transient entity (null ID) should not equal another transient")
        void nullIds_shouldNotBeEqual() {
            Transportation t1 = Transportation.create(istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.create(istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            assertNotEquals(t1, t2);
        }

        @Test
        @DisplayName("Transient entity should not equal persisted entity")
        void nullId_shouldNotEqualPersisted() {
            Transportation transientEntity = Transportation.create(istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation persistedEntity = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());

            assertNotEquals(transientEntity, persistedEntity);
            assertNotEquals(persistedEntity, transientEntity);
        }
    }

    @Nested
    @DisplayName("HashSet Consistency")
    class HashSetConsistencyTests {

        @Test
        @DisplayName("HashSet should remain consistent when entity is added then ID assigned")
        void hashSetConsistency_afterIdAssignment() {
            Transportation transientEntity = Transportation.create(istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            
            Set<Transportation> set = new HashSet<>();
            set.add(transientEntity);

            assertTrue(set.contains(transientEntity));
            assertEquals(1, set.size());
        }

        @Test
        @DisplayName("HashSet should correctly handle multiple persisted entities")
        void hashSet_withPersistedEntities() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(200L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t3 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily()); // Same ID as t1

            Set<Transportation> set = new HashSet<>();
            set.add(t1);
            set.add(t2);
            set.add(t3);

            assertEquals(2, set.size());
            assertTrue(set.contains(t1));
            assertTrue(set.contains(t2));
        }
    }

    @Nested
    @DisplayName("Business Identity")
    class BusinessIdentityTests {

        @Test
        @DisplayName("Same origin, destination, and type should have same business identity")
        void sameBusinessIdentity() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(200L, istanbul, london, TransportationType.FLIGHT, new OperatingDays(1, 2, 3));

            assertNotEquals(t1, t2);
            assertTrue(t1.hasSameBusinessIdentity(t2));
        }

        @Test
        @DisplayName("Different origin should not have same business identity")
        void differentOrigin() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(200L, paris, london, TransportationType.FLIGHT, OperatingDays.daily());

            assertFalse(t1.hasSameBusinessIdentity(t2));
        }

        @Test
        @DisplayName("Different destination should not have same business identity")
        void differentDestination() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(200L, istanbul, paris, TransportationType.FLIGHT, OperatingDays.daily());

            assertFalse(t1.hasSameBusinessIdentity(t2));
        }

        @Test
        @DisplayName("Different type should not have same business identity")
        void differentType() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());
            Transportation t2 = Transportation.reconstruct(200L, istanbul, london, TransportationType.BUS, OperatingDays.daily());

            assertFalse(t1.hasSameBusinessIdentity(t2));
        }

        @Test
        @DisplayName("Null comparison should return false")
        void nullComparison() {
            Transportation t1 = Transportation.reconstruct(100L, istanbul, london, TransportationType.FLIGHT, OperatingDays.daily());

            assertFalse(t1.hasSameBusinessIdentity(null));
        }
    }
}
