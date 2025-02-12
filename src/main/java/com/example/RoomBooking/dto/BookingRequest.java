package com.example.RoomBooking.dto;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    private Long propertyId;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;

}

