package com.example.RoomBooking.dto;

import lombok.*;

import java.sql.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String fullname;
    private String phoneNumber;
    private String gender;
    private Date birthdate;

}

