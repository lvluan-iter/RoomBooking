package com.example.RoomBooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Fullname is required")
    private String fullname;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String gender;
    private Date birthdate;
}