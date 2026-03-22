package com.aviation.routeprovider.api.rest.controller;

import com.aviation.routeprovider.api.rest.dto.request.CreateLocationRequest;
import com.aviation.routeprovider.api.rest.dto.request.UpdateLocationRequest;
import com.aviation.routeprovider.api.rest.dto.response.LocationResponse;
import com.aviation.routeprovider.api.rest.mapper.LocationMapper;
import com.aviation.routeprovider.application.port.in.LocationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Location management operations")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {
    
    private final LocationUseCase locationUseCase;
    private final LocationMapper locationMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    
    @GetMapping
    @Operation(summary = "Get all locations", 
               description = "Returns a paginated list of all locations. Use 'page' and 'size' parameters for pagination.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved locations"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    })
    public ResponseEntity<Page<LocationResponse>> getAllLocations(
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        // Cap page size to prevent memory issues
        // FIX: Ignore sort from client to prevent invalid field errors (e.g., ?sort=string,asc)
        int safePageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safePageSize);
        
        Page<LocationResponse> response = locationUseCase.getAllLocations(safePageable)
            .map(locationMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get location by ID", 
               description = "Returns a single location by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved location"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    public ResponseEntity<LocationResponse> getLocationById(
            @Parameter(description = "Location ID") @PathVariable Long id) {
        LocationResponse response = locationMapper.toResponse(
            locationUseCase.getLocation(id)
        );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create a new location", 
               description = "Creates a new location in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Location created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    })
    public ResponseEntity<LocationResponse> createLocation(
            @Valid @RequestBody CreateLocationRequest request) {
        LocationResponse response = locationMapper.toResponse(
            locationUseCase.createLocation(locationMapper.toCreateCommand(request))
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a location", 
               description = "Updates an existing location. Location code cannot be changed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    public ResponseEntity<LocationResponse> updateLocation(
            @Parameter(description = "Location ID") @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request) {
        LocationResponse response = locationMapper.toResponse(
            locationUseCase.updateLocation(id, locationMapper.toUpdateCommand(request))
        );
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a location", 
               description = "Deletes a location from the system. Cannot delete locations that are referenced by transportations.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete - location is referenced by transportation(s)"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
        @ApiResponse(responseCode = "404", description = "Location not found")
    })
    public ResponseEntity<Void> deleteLocation(
            @Parameter(description = "Location ID") @PathVariable Long id) {
        locationUseCase.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
