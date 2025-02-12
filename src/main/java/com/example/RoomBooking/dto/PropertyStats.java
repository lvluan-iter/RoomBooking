package com.example.RoomBooking.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyStats {
    private Long propertyId;
    private Long views;
    private Long likes;
    private Long requests;

}
