package com.aviation.routeprovider.integration;

import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.repository.LocationJpaRepository;
import com.aviation.routeprovider.infrastructure.persistence.repository.TransportationJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = {"/test-cleanup.sql", "/test-seed.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class LocationIntegrationTest extends PostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationJpaRepository locationRepository;

    @Autowired
    private TransportationJpaRepository transportationRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainJwtToken("admin", "admin123");
    }

    private String obtainJwtToken(String username, String password) throws Exception {
        Map<String, String> loginRequest = Map.of(
            "username", username,
            "password", password
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        return "Bearer " + responseMap.get("token");
    }

    @Nested
    @DisplayName("DELETE /locations/{id} - Reference Validation")
    class DeleteLocationTests {

        @Test
        @DisplayName("Should return 204 when deleting location with no transportation references")
        void shouldDeleteLocationWithNoReferences() throws Exception {
            // Create a location with no transportation references
            LocationJpaEntity location = new LocationJpaEntity();
            location.setName("Unused Airport");
            location.setCountry("Test Country");
            location.setCity("Test City");
            location.setLocationCode("UNUSED");
            location.setCreatedAt(LocalDateTime.now());
            location.setUpdatedAt(LocalDateTime.now());
            location = locationRepository.save(location);

            long countBefore = locationRepository.count();

            mockMvc.perform(delete("/locations/" + location.getId())
                    .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

            // Verify location was deleted
            assertEquals(countBefore - 1, locationRepository.count());
            assertTrue(locationRepository.findById(location.getId()).isEmpty());
        }

        @Test
        @DisplayName("Should return 400 when deleting location referenced as origin")
        void shouldRejectDeleteWhenLocationIsOrigin() throws Exception {
            // Get existing locations from seed data
            LocationJpaEntity origin = locationRepository.findByLocationCode("IST").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("LHR").orElseThrow();

            // Verify transportation exists referencing IST
            long refCount = transportationRepository.countByOriginLocationId(origin.getId())
                + transportationRepository.countByDestinationLocationId(origin.getId());

            // This test depends on seed data having transportations
            if (refCount > 0) {
                mockMvc.perform(delete("/locations/" + origin.getId())
                        .header("Authorization", adminToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("Cannot delete location")))
                    .andExpect(jsonPath("$.message").value(containsString("transportation")));

                // Verify location still exists
                assertTrue(locationRepository.findById(origin.getId()).isPresent());
            }
        }

        @Test
        @DisplayName("Should return 400 when deleting location referenced as destination")
        void shouldRejectDeleteWhenLocationIsDestination() throws Exception {
            // Get existing location that is used as destination
            LocationJpaEntity lhr = locationRepository.findByLocationCode("LHR").orElseThrow();

            long refCount = transportationRepository.countByOriginLocationId(lhr.getId())
                + transportationRepository.countByDestinationLocationId(lhr.getId());

            if (refCount > 0) {
                mockMvc.perform(delete("/locations/" + lhr.getId())
                        .header("Authorization", adminToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Cannot delete location")));

                // Verify location still exists
                assertTrue(locationRepository.findById(lhr.getId()).isPresent());
            }
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent location")
        void shouldReturn404ForNonExistentLocation() throws Exception {
            mockMvc.perform(delete("/locations/99999")
                    .header("Authorization", adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Should include transportation count in error message")
        void shouldIncludeTransportationCountInErrorMessage() throws Exception {
            // Use IST which has multiple transportations in seed data
            LocationJpaEntity ist = locationRepository.findByLocationCode("IST").orElseThrow();

            long refCount = transportationRepository.countByOriginLocationId(ist.getId())
                + transportationRepository.countByDestinationLocationId(ist.getId());

            if (refCount > 0) {
                mockMvc.perform(delete("/locations/" + ist.getId())
                        .header("Authorization", adminToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString(refCount + " transportation")));
            }
        }
    }

    @Nested
    @DisplayName("GET /locations - Pagination")
    class GetLocationsPaginationTests {

        @Test
        @DisplayName("Should return paginated locations")
        void shouldReturnPaginatedLocations() throws Exception {
            mockMvc.perform(get("/locations")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
        }

        @Test
        @DisplayName("Should cap page size to maximum of 100")
        void shouldCapPageSize() throws Exception {
            mockMvc.perform(get("/locations")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "200")) // Request more than max
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(100)); // Capped to 100
        }

        @Test
        @DisplayName("Should use default page size when not specified")
        void shouldUseDefaultPageSize() throws Exception {
            mockMvc.perform(get("/locations")
                    .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(20)); // Default is 20
        }

        @Test
        @DisplayName("Should return second page correctly")
        void shouldReturnSecondPage() throws Exception {
            // First, ensure we have enough data by checking total elements
            MvcResult result = mockMvc.perform(get("/locations")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "1"))
                .andExpect(status().isOk())
                .andReturn();

            String response = result.getResponse().getContentAsString();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            int totalElements = ((Number) responseMap.get("totalElements")).intValue();

            if (totalElements > 1) {
                mockMvc.perform(get("/locations")
                        .header("Authorization", adminToken)
                        .param("page", "1")
                        .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageable.pageNumber").value(1));
            }
        }
    }

    @Nested
    @DisplayName("POST /locations - Create")
    class CreateLocationTests {

        @Test
        @DisplayName("Should create location with valid data")
        void shouldCreateLocation() throws Exception {
            Map<String, String> createRequest = Map.of(
                "name", "New Airport",
                "country", "New Country",
                "city", "New City",
                "locationCode", "NEW"
            );

            mockMvc.perform(post("/locations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Airport"))
                .andExpect(jsonPath("$.country").value("New Country"))
                .andExpect(jsonPath("$.city").value("New City"))
                .andExpect(jsonPath("$.locationCode").value("NEW"));
        }

        @Test
        @DisplayName("Should return 400 when creating location with duplicate code")
        void shouldRejectDuplicateLocationCode() throws Exception {
            // IST already exists from seed data
            Map<String, String> createRequest = Map.of(
                "name", "Another Istanbul Airport",
                "country", "Turkey",
                "city", "Istanbul",
                "locationCode", "IST" // Duplicate
            );

            mockMvc.perform(post("/locations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldRejectMissingRequiredFields() throws Exception {
            Map<String, String> createRequest = Map.of(
                "name", "Incomplete Airport"
                // Missing country, city, locationCode
            );

            mockMvc.perform(post("/locations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /locations/{id} - Update")
    class UpdateLocationTests {

        @Test
        @DisplayName("Should update location with valid data")
        void shouldUpdateLocation() throws Exception {
            LocationJpaEntity location = locationRepository.findByLocationCode("SAW").orElseThrow();

            Map<String, String> updateRequest = Map.of(
                "name", "Updated Sabiha Gokcen",
                "country", "Turkey",
                "city", "Istanbul"
            );

            mockMvc.perform(put("/locations/" + location.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Sabiha Gokcen"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent location")
        void shouldReturn404ForNonExistentLocation() throws Exception {
            Map<String, String> updateRequest = Map.of("name", "Updated Name");

            mockMvc.perform(put("/locations/99999")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when update request has no fields (empty JSON)")
        void shouldRejectEmptyUpdateRequest() throws Exception {
            LocationJpaEntity location = locationRepository.findByLocationCode("SAW").orElseThrow();

            // Empty JSON object - no fields provided
            String emptyJson = "{}";

            mockMvc.perform(put("/locations/" + location.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

            // Verify location was NOT modified
            LocationJpaEntity unchanged = locationRepository.findById(location.getId()).orElseThrow();
            assertEquals("Sabiha Gokcen Airport", unchanged.getName());
        }

        @Test
        @DisplayName("Should return 400 when all update fields are null")
        void shouldRejectNullFieldsUpdateRequest() throws Exception {
            LocationJpaEntity location = locationRepository.findByLocationCode("SAW").orElseThrow();

            // JSON with explicit null values
            String nullFieldsJson = "{\"name\":null,\"country\":null,\"city\":null}";

            mockMvc.perform(put("/locations/" + location.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nullFieldsJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Should allow partial update with single field")
        void shouldAllowPartialUpdateWithSingleField() throws Exception {
            LocationJpaEntity location = locationRepository.findByLocationCode("SAW").orElseThrow();
            String originalCity = location.getCity();

            // Partial update - only name
            Map<String, String> partialUpdate = Map.of("name", "Partially Updated Name");

            mockMvc.perform(put("/locations/" + location.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partially Updated Name"));
        }
    }

    @Nested
    @DisplayName("GET /locations/{id}")
    class GetLocationByIdTests {

        @Test
        @DisplayName("Should return location by ID")
        void shouldReturnLocationById() throws Exception {
            LocationJpaEntity location = locationRepository.findByLocationCode("IST").orElseThrow();

            mockMvc.perform(get("/locations/" + location.getId())
                    .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(location.getId()))
                .andExpect(jsonPath("$.locationCode").value("IST"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent location ID")
        void shouldReturn404ForNonExistentId() throws Exception {
            mockMvc.perform(get("/locations/99999")
                    .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
        }
    }
}
