package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.LocationDTO;
import com.example.RoomBooking.models.Location;
import com.example.RoomBooking.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<LocationDTO> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(this::MapToDTO)
                .collect(Collectors.toList());
    }

    private LocationDTO MapToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setUrl(location.getUrl());
        dto.setDescription(location.getDescription());
        dto.setCount(location.getProperties().size());
        return dto;
    }
}
