package com.example.RoomBooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyRequest {
    private String title;
    private String description;
    private String address;
    private double price;
    private boolean isAvailable;
    private String location;
    private List<String> imageUrls;
    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;
    private List<AmenityDTO> amenities;
    private Long categoryId;
    private Long userId;
    private String furniture;
    private String type;
    private String keywords;
    private List<NearbyPlaceDTO> nearbyPlaces;

    @JsonProperty("isPaid")
    private boolean isPaid;

    private boolean isLocked;
    private boolean isApproved;
    private String reason;
    private Timestamp expirationDate;
}
