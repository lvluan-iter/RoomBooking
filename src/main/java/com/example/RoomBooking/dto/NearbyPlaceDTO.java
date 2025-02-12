package com.example.RoomBooking.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyPlaceDTO {
    private Long id;
    private String name;
    private Double distance;
    private String unit;

}