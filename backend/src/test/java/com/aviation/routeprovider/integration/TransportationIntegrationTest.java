package com.aviation.routeprovider.integration;

import com.aviation.routeprovider.domain.model.valueobject.TransportationType;
import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.entity.TransportationJpaEntity;
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
class TransportationIntegrationTest extends PostgreSQLIntegrationTest {

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
    @DisplayName("GET /transportations - Pagination")
    class GetTransportationsPaginationTests {

        @Test
        @DisplayName("Should return paginated transportations")
        void shouldReturnPaginatedTransportations() throws Exception {
            mockMvc.perform(get("/transportations")
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
            mockMvc.perform(get("/transportations")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "200")) // Request more than max
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(100)); // Capped to 100
        }

        @Test
        @DisplayName("Should use default page size when not specified")
        void shouldUseDefaultPageSize() throws Exception {
            mockMvc.perform(get("/transportations")
                    .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(20)); // Default is 20
        }

        @Test
        @DisplayName("Should include transportation details with locations in response")
        void shouldIncludeTransportationDetailsWithLocations() throws Exception {
            mockMvc.perform(get("/transportations")
                    .header("Authorization", adminToken)
                    .param("page", "0")
                    .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isNumber())
                .andExpect(jsonPath("$.content[0].transportationType").isString())
                .andExpect(jsonPath("$.content[0].operatingDays").isArray())
                .andExpect(jsonPath("$.content[0].originLocation").exists())
                .andExpect(jsonPath("$.content[0].destinationLocation").exists());
        }

        @Test
        @DisplayName("Should return empty content for page beyond available data")
        void shouldReturnEmptyForPageBeyondData() throws Exception {
            mockMvc.perform(get("/transportations")
                    .header("Authorization", adminToken)
                    .param("page", "999")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalPages").isNumber());
        }
    }

    @Nested
    @DisplayName("POST /transportations - Create")
    class CreateTransportationTests {

        @Test
        @DisplayName("Should create transportation with valid data")
        void shouldCreateTransportation() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("IST").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("LHR").orElseThrow();

            Map<String, Object> createRequest = Map.of(
                "originLocationId", origin.getId(),
                "destinationLocationId", destination.getId(),
                "transportationType", "FLIGHT",
                "operatingDays", new int[]{1, 2, 3, 4, 5}
            );

            mockMvc.perform(post("/transportations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.transportationType").value("FLIGHT"))
                .andExpect(jsonPath("$.operatingDays", hasSize(5)));
        }

        @Test
        @DisplayName("Should return 400 when origin location does not exist")
        void shouldRejectNonExistentOrigin() throws Exception {
            LocationJpaEntity destination = locationRepository.findByLocationCode("LHR").orElseThrow();

            Map<String, Object> createRequest = Map.of(
                "originLocationId", 99999L, // Non-existent
                "destinationLocationId", destination.getId(),
                "transportationType", "FLIGHT",
                "operatingDays", new int[]{1, 2, 3}
            );

            mockMvc.perform(post("/transportations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when destination location does not exist")
        void shouldRejectNonExistentDestination() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("IST").orElseThrow();

            Map<String, Object> createRequest = Map.of(
                "originLocationId", origin.getId(),
                "destinationLocationId", 99999L, // Non-existent
                "transportationType", "FLIGHT",
                "operatingDays", new int[]{1, 2, 3}
            );

            mockMvc.perform(post("/transportations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 for invalid transportation type")
        void shouldRejectInvalidTransportationType() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("IST").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("LHR").orElseThrow();

            Map<String, Object> createRequest = Map.of(
                "originLocationId", origin.getId(),
                "destinationLocationId", destination.getId(),
                "transportationType", "INVALID_TYPE",
                "operatingDays", new int[]{1, 2, 3}
            );

            mockMvc.perform(post("/transportations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should create transportation with different types")
        void shouldCreateDifferentTransportationTypes() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("CCTAK").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            Map<String, Object> createRequest = Map.of(
                "originLocationId", origin.getId(),
                "destinationLocationId", destination.getId(),
                "transportationType", "UBER",
                "operatingDays", new int[]{1, 2, 3, 4, 5, 6, 7}
            );

            mockMvc.perform(post("/transportations")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transportationType").value("UBER"));
        }
    }

    @Nested
    @DisplayName("PUT /transportations/{id} - Update")
    class UpdateTransportationTests {

        @Test
        @DisplayName("Should update transportation type")
        void shouldUpdateTransportationType() throws Exception {
            // Create a test transportation first
            LocationJpaEntity origin = locationRepository.findByLocationCode("SAW").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            TransportationJpaEntity transportation = new TransportationJpaEntity();
            transportation.setOriginLocation(origin);
            transportation.setDestinationLocation(destination);
            transportation.setTransportationType(TransportationType.BUS);
            transportation.setOperatingDays(new int[]{1, 2, 3, 4, 5});
            transportation.setCreatedAt(LocalDateTime.now());
            transportation.setUpdatedAt(LocalDateTime.now());
            transportation = transportationRepository.save(transportation);

            Map<String, Object> updateRequest = Map.of(
                "transportationType", "FLIGHT"
            );

            mockMvc.perform(put("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transportationType").value("FLIGHT"));
        }

        @Test
        @DisplayName("Should update operating days")
        void shouldUpdateOperatingDays() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("SAW").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            TransportationJpaEntity transportation = new TransportationJpaEntity();
            transportation.setOriginLocation(origin);
            transportation.setDestinationLocation(destination);
            transportation.setTransportationType(TransportationType.FLIGHT);
            transportation.setOperatingDays(new int[]{1, 2, 3});
            transportation.setCreatedAt(LocalDateTime.now());
            transportation.setUpdatedAt(LocalDateTime.now());
            transportation = transportationRepository.save(transportation);

            Map<String, Object> updateRequest = Map.of(
                "operatingDays", new int[]{1, 2, 3, 4, 5, 6, 7}
            );

            mockMvc.perform(put("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operatingDays", hasSize(7)));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent transportation")
        void shouldReturn404ForNonExistentTransportation() throws Exception {
            Map<String, Object> updateRequest = Map.of(
                "transportationType", "FLIGHT"
            );

            mockMvc.perform(put("/transportations/99999")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when update request has no fields (empty JSON)")
        void shouldRejectEmptyUpdateRequest() throws Exception {
            // Create a test transportation first
            LocationJpaEntity origin = locationRepository.findByLocationCode("SAW").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            TransportationJpaEntity transportation = new TransportationJpaEntity();
            transportation.setOriginLocation(origin);
            transportation.setDestinationLocation(destination);
            transportation.setTransportationType(TransportationType.BUS);
            transportation.setOperatingDays(new int[]{1, 2, 3, 4, 5});
            transportation.setCreatedAt(LocalDateTime.now());
            transportation.setUpdatedAt(LocalDateTime.now());
            transportation = transportationRepository.save(transportation);

            // Empty JSON object - no fields provided
            String emptyJson = "{}";

            mockMvc.perform(put("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

            // Verify transportation was NOT modified
            TransportationJpaEntity unchanged = transportationRepository.findById(transportation.getId()).orElseThrow();
            assertEquals(TransportationType.BUS, unchanged.getTransportationType());
        }

        @Test
        @DisplayName("Should return 400 when all update fields are null")
        void shouldRejectNullFieldsUpdateRequest() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("SAW").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            TransportationJpaEntity transportation = new TransportationJpaEntity();
            transportation.setOriginLocation(origin);
            transportation.setDestinationLocation(destination);
            transportation.setTransportationType(TransportationType.BUS);
            transportation.setOperatingDays(new int[]{1, 2, 3});
            transportation.setCreatedAt(LocalDateTime.now());
            transportation.setUpdatedAt(LocalDateTime.now());
            transportation = transportationRepository.save(transportation);

            // JSON with explicit null values
            String nullFieldsJson = "{\"transportationType\":null,\"operatingDays\":null}";

            mockMvc.perform(put("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nullFieldsJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Should return 400 when operating days array is empty")
        void shouldRejectEmptyOperatingDaysArray() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("SAW").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            TransportationJpaEntity transportation = new TransportationJpaEntity();
            transportation.setOriginLocation(origin);
            transportation.setDestinationLocation(destination);
            transportation.setTransportationType(TransportationType.BUS);
            transportation.setOperatingDays(new int[]{1, 2, 3});
            transportation.setCreatedAt(LocalDateTime.now());
            transportation.setUpdatedAt(LocalDateTime.now());
            transportation = transportationRepository.save(transportation);

            // Empty operating days array - should fail @AtLeastOneField
            String emptyArrayJson = "{\"operatingDays\":[]}";

            mockMvc.perform(put("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(emptyArrayJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("DELETE /transportations/{id}")
    class DeleteTransportationTests {

        @Test
        @DisplayName("Should delete existing transportation")
        void shouldDeleteTransportation() throws Exception {
            LocationJpaEntity origin = locationRepository.findByLocationCode("SAW").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("IST").orElseThrow();

            TransportationJpaEntity transportation = new TransportationJpaEntity();
            transportation.setOriginLocation(origin);
            transportation.setDestinationLocation(destination);
            transportation.setTransportationType(TransportationType.BUS);
            transportation.setOperatingDays(new int[]{1, 2, 3});
            transportation.setCreatedAt(LocalDateTime.now());
            transportation.setUpdatedAt(LocalDateTime.now());
            transportation = transportationRepository.save(transportation);

            mockMvc.perform(delete("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

            // Verify deleted
            assertTrue(transportationRepository.findById(transportation.getId()).isEmpty());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent transportation")
        void shouldReturn404ForNonExistentTransportation() throws Exception {
            mockMvc.perform(delete("/transportations/99999")
                    .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /transportations/{id}")
    class GetTransportationByIdTests {

        @Test
        @DisplayName("Should return transportation by ID")
        void shouldReturnTransportationById() throws Exception {
            // Get an existing transportation from seed data
            TransportationJpaEntity transportation = transportationRepository.findAll().get(0);

            mockMvc.perform(get("/transportations/" + transportation.getId())
                    .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transportation.getId()))
                .andExpect(jsonPath("$.transportationType").isString());
        }

        @Test
        @DisplayName("Should return 404 for non-existent transportation ID")
        void shouldReturn404ForNonExistentId() throws Exception {
            mockMvc.perform(get("/transportations/99999")
                    .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
        }
    }
}
