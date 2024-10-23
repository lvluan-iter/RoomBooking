package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.BookingRequest;
import com.example.RoomBooking.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveProperty(@RequestBody BookingRequest bookingRequest) {
        bookingService.reserveProperty(bookingRequest);
        return ResponseEntity.ok("Property reserved successfully.");
    }
}
