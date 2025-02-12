package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.AmenityDTO;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Amenity;
import com.example.RoomBooking.repositories.AmenityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmenityService {
    private final AmenityRepository amenityRepository;

    public AmenityService(AmenityRepository amenityRepository) {
        this.amenityRepository = amenityRepository;
    }

    public List<AmenityDTO> getAllAmenities() {
        List<Amenity> amenities = amenityRepository.findAll();
        return amenities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AmenityDTO getAmenityById(Long id) {
        return amenityRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id:" + id));
    }

    public AmenityDTO createAmenity(AmenityDTO amenityDTO) {
        Amenity amenity = new Amenity();
        updateAmenityFromDTO(amenity, amenityDTO);
        Amenity savedAmenity = amenityRepository.save(amenity);
        return mapToResponse(savedAmenity);
    }

    public AmenityDTO updateAmenity(AmenityDTO amenityDTO) {
        Amenity existingAmenity = amenityRepository.findById(amenityDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id:" + amenityDTO.getId()));

        updateAmenityFromDTO(existingAmenity, amenityDTO);
        Amenity updatedAmenity = amenityRepository.save(existingAmenity);
        return mapToResponse(updatedAmenity);
    }

    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Amenity not found with id:" + id);
        }
        amenityRepository.deleteById(id);
    }

    private AmenityDTO mapToResponse(Amenity amenity) {
        AmenityDTO response = new AmenityDTO();
        response.setId(amenity.getId());
        response.setName(amenity.getName());
        response.setIcon(amenity.getIcon());
        return response;
    }

    private void updateAmenityFromDTO(Amenity amenity, AmenityDTO amenityDTO) {
        amenity.setName(amenityDTO.getName());
        amenity.setIcon(amenityDTO.getIcon());
    }
}