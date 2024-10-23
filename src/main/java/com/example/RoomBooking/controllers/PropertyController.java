package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.services.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    @GetMapping("/available")
    public ResponseEntity<Page<PropertyResponse>> getAvailableProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        Page<PropertyResponse> properties = propertyService.getAvailableProperties(PageRequest.of(page, size));
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PropertyResponse>> getTop10PopularProperties() {
        List<PropertyResponse> popular = propertyService.getTop10PopularProperties();
        return ResponseEntity.ok(popular);
    }

    @PostMapping("/search")
    public ResponseEntity<Page<PropertyResponse>> searchProperties(
            @RequestBody PropertySearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PropertyResponse> properties = propertyService.searchProperties(searchDTO, PageRequest.of(page, size));
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/searchNearby")
    public ResponseEntity<?> searchNearbyProperties(@RequestParam String location,
                                                    @RequestParam Long propertyId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PropertyResponse> properties = propertyService.searchNearBy(location, propertyId, pageable);
            return ResponseEntity.ok(properties);
        } catch (RuntimeException e) {
            // Trả về một ResponseEntity với HttpStatus.BAD_REQUEST và thông báo lỗi dưới dạng JSON
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        PropertyResponse property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @PostMapping
    public ResponseEntity<?> addProperty(@RequestBody PropertyRequest propertyRequest) {
        propertyService.addProperty(propertyRequest);
        return ResponseEntity.ok("Property added successfully.");
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<?> updateProperty(@PathVariable Long propertyId, @RequestBody PropertyRequest propertyRequest) {
        propertyService.updateProperty(propertyId, propertyRequest);
        return ResponseEntity.ok("Property updated successfully.");
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok("Property deleted successfully.");
    }

    @GetMapping("/quick-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserQuickStats(@PathVariable Long userId) {
        Map<String, Object> quickStats = propertyService.getQuickStatsForUser(userId);
        return ResponseEntity.ok(quickStats);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<List<PropertyStats>> getPropertyStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        List<PropertyStats> stats = propertyService.getPropertyStatsForUser(userId, yearMonth);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<PropertyResponse>> getPropertyForUser(@PathVariable Long userId) {
        List<PropertyResponse> responses = propertyService.getPropertyForUser(userId);
        return ResponseEntity.ok(responses);
    }
}

