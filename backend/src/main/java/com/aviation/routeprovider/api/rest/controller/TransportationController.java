package com.aviation.routeprovider.api.rest.controller;

import com.aviation.routeprovider.api.rest.dto.request.CreateTransportationRequest;
import com.aviation.routeprovider.api.rest.dto.request.UpdateTransportationRequest;
import com.aviation.routeprovider.api.rest.dto.response.TransportationResponse;
import com.aviation.routeprovider.api.rest.mapper.TransportationMapper;
import com.aviation.routeprovider.application.port.in.TransportationUseCase;
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

/**
 * REST Controller for Transportation operations.
 * 
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/transportations")
@RequiredArgsConstructor
@Tag(name = "Transportations", description = "Transportation management operations")
@SecurityRequirement(name = "bearerAuth")
public class TransportationController {
    
    private final TransportationUseCase transportationUseCase;
    private final TransportationMapper transportationMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @GetMapping
    @Operation(summary = "Get all transportations", 
               description = "Returns a paginated list of all transportations. Use 'page' and 'size' parameters for pagination.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transportations"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    })
    public ResponseEntity<Page<TransportationResponse>> getAllTransportations(
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        int safePageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safePageSize);
        
        Page<TransportationResponse> response = transportationUseCase.getAllTransportations(safePageable)
            .map(transportationMapper::toResponse);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get transportation by ID", 
               description = "Returns a single transportation by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transportation"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
        @ApiResponse(responseCode = "404", description = "Transportation not found")
    })
    public ResponseEntity<TransportationResponse> getTransportationById(
            @Parameter(description = "Transportation ID") @PathVariable Long id) {
        TransportationResponse response = transportationMapper.toResponse(
            transportationUseCase.getTransportation(id)
        );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create a new transportation", 
               description = "Creates a new transportation in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transportation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    })
    public ResponseEntity<TransportationResponse> createTransportation(
            @Valid @RequestBody CreateTransportationRequest request) {
        TransportationResponse response = transportationMapper.toResponse(
            transportationUseCase.createTransportation(
                transportationMapper.toCreateCommand(request))
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a transportation", 
               description = "Updates an existing transportation. Origin and destination locations cannot be changed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transportation updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
        @ApiResponse(responseCode = "404", description = "Transportation not found")
    })
    public ResponseEntity<TransportationResponse> updateTransportation(
            @Parameter(description = "Transportation ID") @PathVariable Long id,
            @Valid @RequestBody UpdateTransportationRequest request) {
        TransportationResponse response = transportationMapper.toResponse(
            transportationUseCase.updateTransportation(
                id, transportationMapper.toUpdateCommand(request))
        );
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transportation", 
               description = "Deletes a transportation from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Transportation deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)"),
        @ApiResponse(responseCode = "404", description = "Transportation not found")
    })
    public ResponseEntity<Void> deleteTransportation(
            @Parameter(description = "Transportation ID") @PathVariable Long id) {
        transportationUseCase.deleteTransportation(id);
        return ResponseEntity.noContent().build();
    }
}
