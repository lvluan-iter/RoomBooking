package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.AmenityDTO;
import com.example.RoomBooking.services.AmenityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @GetMapping
    public ResponseEntity<List<AmenityDTO>> getAllAmenities() {
        List<AmenityDTO> amenities = amenityService.getAllAmenities();
        return ResponseEntity.ok(amenities);
    }

    @PostMapping
    public ResponseEntity<AmenityDTO> createAmenity(@RequestBody AmenityDTO amenityDTO) {
        AmenityDTO createdAmenity = amenityService.createAmenity(amenityDTO);
        return ResponseEntity.ok(createdAmenity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmenityDTO> updateAmenity(
            @PathVariable Long id,
            @RequestBody AmenityDTO amenityDTO) {
        amenityDTO.setId(id);
        AmenityDTO updatedAmenity = amenityService.updateAmenity(amenityDTO);
        return ResponseEntity.ok(updatedAmenity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityDTO> getAmenityById(@PathVariable Long id) {
        AmenityDTO amenity = amenityService.getAmenityById(id);
        return ResponseEntity.ok(amenity);
    }
}