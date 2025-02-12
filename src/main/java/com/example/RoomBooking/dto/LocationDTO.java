package com.example.RoomBooking.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDTO {
    Long id;
    String name;
    String url;
    String description;
    Integer count;

}
