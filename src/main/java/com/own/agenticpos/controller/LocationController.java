package com.own.agenticpos.controller;

import com.own.agenticpos.dto.LocationResponse;
import com.own.agenticpos.dto.LocationCreateRequest;
import com.own.agenticpos.service.LocationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/locations")
@Validated
@RequiredArgsConstructor
public class LocationController {
    
    private final LocationService locationService;
    
    /**
     * Get all locations
     * GET /api/v1/locations
     */
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> locations = locationService.getAllActiveLocations();
        return ResponseEntity.ok(locations);
    }
    
    /**
     * Get location by ID
     * GET /api/v1/locations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable @Min(1) Long id) {
        Optional<LocationResponse> location = locationService.getLocationById(id);
        return location.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create new location
     * POST /api/v1/locations
     */
    @PostMapping
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationCreateRequest request) {
        LocationResponse createdLocation = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }
}
