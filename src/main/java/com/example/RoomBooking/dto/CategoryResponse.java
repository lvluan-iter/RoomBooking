package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Property;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String categoryName;
    private String imageUrl;
    private List<Property> properties;

}
