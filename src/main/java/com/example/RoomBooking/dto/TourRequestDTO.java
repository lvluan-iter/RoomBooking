package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.TourRequest;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRequestDTO {
    private Long id;
    private Long propertyId;
    private String title;
    private String url;
    private String phoneNumber;
    private String email;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private TourRequest.TourStatus status;
    private LocalDateTime createdAt;

}
