package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.TourRequestDTO;
import com.example.RoomBooking.models.TourRequest;
import com.example.RoomBooking.services.TourRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tour-requests")
public class TourRequestController {
    private final TourRequestService tourRequestService;

    @PostMapping
    public ResponseEntity<?> createTourRequest(@RequestBody TourRequestDTO tourRequest) {
        TourRequestDTO createdRequest = tourRequestService.createTourRequest(tourRequest);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllTourRequests() {
        List<TourRequestDTO> tourRequests = tourRequestService.getAllTourRequests();
        return ResponseEntity.ok(tourRequests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTourRequestById(@PathVariable Long id) {
        return tourRequestService.getTourRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<?> getTourRequestsByPropertyId(@PathVariable Long propertyId) {
        List<TourRequestDTO> tourRequests = tourRequestService.getTourRequestsByPropertyId(propertyId);
        return ResponseEntity.ok(tourRequests);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getTourRequestsByEmail(@PathVariable String email) {
        List<TourRequestDTO> tourRequests = tourRequestService.getTourRequestsByEmail(email);
        return ResponseEntity.ok(tourRequests);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTourRequestsByUserId(@PathVariable Long userId) {
        List<TourRequestDTO> requests = tourRequestService.getTourRequestsByUserId(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTourRequestsByStatus(@PathVariable TourRequest.TourStatus status) {
        List<TourRequestDTO> tourRequests = tourRequestService.getTourRequestsByStatus(status);
        return ResponseEntity.ok(tourRequests);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTourRequestStatus(
            @PathVariable Long id,
            @RequestBody TourRequest.TourStatus status
    ) {
        return tourRequestService.updateTourRequestStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/date")
    public ResponseEntity<?> updateTourRequestDate(
            @PathVariable Long id,
            @RequestBody TourRequestDTO dto
    ) {
        return tourRequestService.updateTourRequestDate(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTourRequest(@PathVariable Long id) {
        boolean deleted = tourRequestService.deleteTourRequest(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}