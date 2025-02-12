package com.example.RoomBooking.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String fullname;
    private String phoneNumber;
    private String avatar;

}
