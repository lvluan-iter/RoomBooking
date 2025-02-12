package com.example.RoomBooking.models;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatus {
    private String status;
    private Long lastUpdated;

}