package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.LocationDTO;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Location;
import com.example.RoomBooking.repositories.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationDTO createLocation(LocationDTO locationDTO) {
        Location location = Location.builder()
                .name(locationDTO.getName())
                .url(locationDTO.getUrl())
                .description(locationDTO.getDescription())
                .build();

        Location savedLocation = locationRepository.save(location);
        return MapToDTO(savedLocation);
    }

    public List<LocationDTO> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream()
                .map(this::MapToDTO)
                .collect(Collectors.toList());
    }

    public LocationDTO getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return MapToDTO(location);
    }

    public LocationDTO updateLocation(Long id, LocationDTO locationDTO) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        location.setName(locationDTO.getName());
        location.setUrl(locationDTO.getUrl());
        location.setDescription(locationDTO.getDescription());

        Location updated = locationRepository.save(location);
        return MapToDTO(updated);
    }

    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
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
