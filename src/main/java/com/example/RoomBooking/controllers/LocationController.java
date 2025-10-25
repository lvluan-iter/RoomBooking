package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.LocationDTO;
import com.example.RoomBooking.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/location")
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> createLocation(@RequestBody LocationDTO locationDTO) {
        LocationDTO createdLocation = locationService.createLocation(locationDTO);
        return ResponseEntity.ok(createdLocation);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getAllLocations() {
        List<LocationDTO> response = locationService.getAllLocations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        LocationDTO location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> updateLocation(
            @PathVariable Long id,
            @RequestBody LocationDTO locationDTO) {
        LocationDTO updatedLocation = locationService.updateLocation(id, locationDTO);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Owner', 'Admin')")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}