package com.example.RoomBooking.dto;

import lombok.*;

import java.sql.Date;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String username;
    private String email;
    private String password;
    private String fullname;
    private String phoneNumber;
    private String gender;
    private Date birthdate;
    private String avatar;
    private Set<Long> roleIds;
    private Set<Long> favoritePropertyIds;
    private boolean isPublicProfile;
    private boolean isPublicPhone;
    private boolean isPublicEmail;
}
