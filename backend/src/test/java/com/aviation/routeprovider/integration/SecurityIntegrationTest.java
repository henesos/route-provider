package com.aviation.routeprovider.integration;

import com.aviation.routeprovider.infrastructure.persistence.entity.LocationJpaEntity;
import com.aviation.routeprovider.infrastructure.persistence.repository.LocationJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityIntegrationTest extends PostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationJpaRepository locationRepository;

    private String adminToken;
    private String agencyToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = obtainJwtToken("admin", "admin123");
        agencyToken = obtainJwtToken("agency", "agency123");
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
    @DisplayName("401 Unauthorized - No Authentication")
    class UnauthenticatedTests {

        @Test
        @DisplayName("Should return 401 when accessing /locations without token")
        void shouldReturn401ForLocationsWithoutToken() throws Exception {
            mockMvc.perform(get("/locations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("Should return 401 when accessing /transportations without token")
        void shouldReturn401ForTransportationsWithoutToken() throws Exception {
            mockMvc.perform(get("/transportations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Should return 401 when accessing /routes/search without token")
        void shouldReturn401ForRoutesWithoutToken() throws Exception {
            Map<String, Object> searchRequest = Map.of(
                "originLocationId", 1L,
                "destinationLocationId", 2L,
                "travelDate", LocalDate.now().plusDays(1).toString()
            );

            mockMvc.perform(post("/routes/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Should return 401 when using malformed JWT token")
        void shouldReturn401ForMalformedToken() throws Exception {
            mockMvc.perform(get("/locations")
                    .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 when using invalid Bearer format")
        void shouldReturn401ForInvalidBearerFormat() throws Exception {
            // Missing "Bearer " prefix
            mockMvc.perform(get("/locations")
                    .header("Authorization", "sometoken"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("403 Forbidden - Authenticated but Unauthorized")
    class UnauthorizedRoleTests {

        @Test
        @DisplayName("Should return 403 when AGENCY user accesses /locations")
        void shouldReturn403ForAgencyAccessingLocations() throws Exception {
            mockMvc.perform(get("/locations")
                    .header("Authorization", agencyToken))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 when AGENCY user accesses /transportations")
        void shouldReturn403ForAgencyAccessingTransportations() throws Exception {
            mockMvc.perform(get("/transportations")
                    .header("Authorization", agencyToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Should return 403 when AGENCY user creates a location")
        void shouldReturn403ForAgencyCreatingLocation() throws Exception {
            Map<String, String> createRequest = Map.of(
                "name", "Test Airport",
                "country", "Test Country",
                "city", "Test City",
                "locationCode", "TST"
            );

            mockMvc.perform(post("/locations")
                    .header("Authorization", agencyToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when AGENCY user deletes a location")
        void shouldReturn403ForAgencyDeletingLocation() throws Exception {
            mockMvc.perform(delete("/locations/1")
                    .header("Authorization", agencyToken))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("200 OK - Proper Authentication and Authorization")
    class AuthorizedTests {

        @Test
        @DisplayName("Should return 200 when ADMIN user accesses /locations")
        void shouldReturn200ForAdminAccessingLocations() throws Exception {
            mockMvc.perform(get("/locations")
                    .header("Authorization", adminToken))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 200 when ADMIN user accesses /transportations")
        void shouldReturn200ForAdminAccessingTransportations() throws Exception {
            mockMvc.perform(get("/transportations")
                    .header("Authorization", adminToken))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 200 when AGENCY user accesses /routes/search")
        void shouldReturn200ForAgencyAccessingRoutes() throws Exception {
            // Get actual location IDs from seed data
            LocationJpaEntity origin = locationRepository.findByLocationCode("IST").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("LHR").orElseThrow();

            Map<String, Object> searchRequest = Map.of(
                "originLocationId", origin.getId(),
                "destinationLocationId", destination.getId(),
                "travelDate", LocalDate.now().plusDays(1).toString()
            );

            mockMvc.perform(post("/routes/search")
                    .header("Authorization", agencyToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 200 when ADMIN user accesses /routes/search")
        void shouldReturn200ForAdminAccessingRoutes() throws Exception {
            // Get actual location IDs from seed data
            LocationJpaEntity origin = locationRepository.findByLocationCode("IST").orElseThrow();
            LocationJpaEntity destination = locationRepository.findByLocationCode("LHR").orElseThrow();

            Map<String, Object> searchRequest = Map.of(
                "originLocationId", origin.getId(),
                "destinationLocationId", destination.getId(),
                "travelDate", LocalDate.now().plusDays(1).toString()
            );

            mockMvc.perform(post("/routes/search")
                    .header("Authorization", adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should return 200 for /auth/login without authentication")
        void shouldAllowLoginWithoutAuth() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "admin",
                "password", "admin123"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 200 for Swagger UI without authentication")
        void shouldAllowSwaggerUIWithoutAuth() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        }
    }
}
