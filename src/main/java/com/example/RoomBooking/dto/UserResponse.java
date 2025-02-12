package com.example.RoomBooking.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullname;
    private String phoneNumber;
    private String gender;
    private Date birthdate;
    private String avatar;
    private Set<String> roles;
    private Set<Long> favoritePropertyIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPublicProfile;
    private boolean isPublicPhone;
    private boolean isPublicEmail;
}

