package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.AmenityDTO;
import com.example.RoomBooking.models.Amenity;
import com.example.RoomBooking.repositories.AmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmenityService {

    @Autowired
    private AmenityRepository amenityRepository;

    public List<AmenityDTO> getAllAmenities() {
        List<Amenity> amenities = amenityRepository.findAll();
        return amenities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AmenityDTO mapToResponse(Amenity amenity) {
        AmenityDTO response = new AmenityDTO();
        response.setId(amenity.getId());
        response.setName(amenity.getName());
        response.setIcon(amenity.getIcon());
        return response;
    }
}
