package com.example.RoomBooking.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySearchDTO {
    private String type;
    private String keyword;
    private Double minPrice;
    private Double maxPrice;
    private String location;
    private Long categoryId;
    private Double minArea;
    private Double maxArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private List<Long> amenities;
    private boolean isAvailable;
}
