package com.example.RoomBooking.models;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdate {
    private Long userId;
    private UserStatus status;
}