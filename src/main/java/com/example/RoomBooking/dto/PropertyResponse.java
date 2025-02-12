package com.example.RoomBooking.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {
    private Long id;
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
    private String categoryName;
    private UserDTO user;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Double visits;
    private String furniture;
    private String type;
    private String keywords;
    private List<NearbyPlaceDTO> nearbyPlaces;
    private Timestamp expirationDate;
    private boolean isApproved;
    private boolean isPaid;
    private boolean isLocked;
    private String reason;
}
