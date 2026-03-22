package com.aviation.routeprovider.api.rest.controller;

import com.aviation.routeprovider.api.rest.dto.request.RouteSearchRequest;
import com.aviation.routeprovider.api.rest.dto.response.RouteResponse;
import com.aviation.routeprovider.api.rest.mapper.RouteMapper;
import com.aviation.routeprovider.application.port.in.RouteSearchQuery;
import com.aviation.routeprovider.application.port.in.RouteSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Route search operations")
@SecurityRequirement(name = "bearerAuth")
public class RouteController {
    
    private final RouteSearchUseCase routeSearchUseCase;
    private final RouteMapper routeMapper;
    
    @PostMapping("/search")
    @Operation(summary = "Search for valid routes", 
               description = "Searches for all valid routes from origin to destination on a given date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved routes"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    public ResponseEntity<List<RouteResponse>> searchRoutes(
            @Valid @RequestBody RouteSearchRequest request) {
        RouteSearchQuery query = routeMapper.toQuery(request);
        List<RouteResponse> response = routeSearchUseCase.searchRoutes(query);
        return ResponseEntity.ok(response);
    }
}
