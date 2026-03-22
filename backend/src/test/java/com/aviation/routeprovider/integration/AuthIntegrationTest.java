package com.aviation.routeprovider.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends PostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 with JWT token for valid admin credentials")
        void shouldReturnTokenForValidAdminCredentials() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "admin",
                "password", "admin123"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.username").value("admin"));
        }

        @Test
        @DisplayName("Should return 200 with JWT token for valid agency credentials")
        void shouldReturnTokenForValidAgencyCredentials() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "agency",
                "password", "agency123"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("AGENCY"))
                .andExpect(jsonPath("$.username").value("agency"));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "admin",
                "password", "wrongpassword"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
        }

        @Test
        @DisplayName("Should return 401 for non-existent user")
        void shouldReturn401ForNonExistentUser() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "nonexistent",
                "password", "password123"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing username")
        void shouldReturn400ForMissingUsername() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "password", "admin123"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing password")
        void shouldReturn400ForMissingPassword() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "admin"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for short username")
        void shouldReturn400ForShortUsername() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "ab", // Less than 3 characters
                "password", "admin123"
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for short password")
        void shouldReturn400ForShortPassword() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "admin",
                "password", "12345" // Less than 6 characters
            );

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return expiresIn matching configuration")
        void shouldReturnConfiguredExpiration() throws Exception {
            Map<String, String> loginRequest = Map.of(
                "username", "admin",
                "password", "admin123"
            );

            MvcResult result = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn();

            String response = result.getResponse().getContentAsString();
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            // Default expiration is 86400000ms (24 hours)
            Long expiresIn = ((Number) responseMap.get("expiresIn")).longValue();
            assertTrue(expiresIn > 0, "expiresIn should be positive");
            assertTrue(expiresIn <= 86400000L, "expiresIn should not exceed configured value");
        }
    }
}
