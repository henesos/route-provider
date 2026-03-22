package com.aviation.routeprovider.api.security;

import com.aviation.routeprovider.api.rest.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * FIX: Explicit security exception handlers for consistent 401/403 JSON responses.
 * 
 * Without these handlers, Spring Security returns default HTML error pages
 * or uses default behavior that doesn't integrate with our API error format.
 * 
 * These handlers ensure:
 * - 401 UNAUTHORIZED: Missing or invalid authentication
 * - 403 FORBIDDEN: Authenticated but lacking required role
 */
@Slf4j
public class SecurityExceptionHandlers {

    private SecurityExceptionHandlers() {
        // Utility class
    }

    /**
     * Handles authentication failures (401 UNAUTHORIZED).
     * 
     * Triggered when:
     * - No JWT token provided for protected endpoint
     * - Invalid/expired JWT token
     * - Bad credentials during login
     */
    @Component
    @RequiredArgsConstructor
    public static class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

        private final ObjectMapper objectMapper;

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                           AuthenticationException authException) throws IOException {
            
            log.warn("Authentication failed for {}: {}", request.getRequestURI(), authException.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ErrorResponse errorResponse = ErrorResponse.of(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "Authentication required. Please provide a valid JWT token.",
                request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    /**
     * Handles authorization failures (403 FORBIDDEN).
     * 
     * Triggered when:
     * - User is authenticated but lacks required role
     * - e.g., AGENCY user trying to access ADMIN-only endpoints
     */
    @Component
    @RequiredArgsConstructor
    public static class JwtAccessDeniedHandler implements AccessDeniedHandler {

        private final ObjectMapper objectMapper;

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                          AccessDeniedException accessDeniedException) throws IOException {
            
            log.warn("Access denied for {}: {}", request.getRequestURI(), accessDeniedException.getMessage());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            ErrorResponse errorResponse = ErrorResponse.of(
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "Access denied. You don't have permission to access this resource.",
                request.getRequestURI()
            );

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
