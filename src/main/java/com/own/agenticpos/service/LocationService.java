package com.own.agenticpos.service;

import com.own.agenticpos.dto.LocationCreateRequest;
import com.own.agenticpos.dto.LocationResponse;
import com.own.agenticpos.entity.Location;
import com.own.agenticpos.exception.DuplicateLocationException;
import com.own.agenticpos.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationService {
    
    private final LocationRepository locationRepository;
    
    /**
     * Get all active locations
     */
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllActiveLocations() {
        List<Location> locations = locationRepository.findAllActiveOrderByName();
        return locations.stream()
                .map(LocationResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get location by ID
     */
    @Transactional(readOnly = true)
    public Optional<LocationResponse> getLocationById(Long id) {
        return locationRepository.findByIdAndActiveTrue(id)
                .map(LocationResponse::from);
    }
    
    /**
     * Create new location
     */
    public LocationResponse createLocation(LocationCreateRequest request) {
        // Check if location name already exists
        Optional<Location> existingLocation = locationRepository.findByNameAndActiveTrue(request.getName());
        if (existingLocation.isPresent()) {
            throw new DuplicateLocationException("Location with name '" + request.getName() + "' already exists");
        }
        
        Location location = Location.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .active(true)
                .build();
        
        location = locationRepository.save(location);
        return LocationResponse.from(location);
    }
}
