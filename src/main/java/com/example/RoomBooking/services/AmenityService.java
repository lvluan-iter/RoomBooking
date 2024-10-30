package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.AmenityDTO;
import com.example.RoomBooking.models.Amenity;
import com.example.RoomBooking.repositories.AmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    public AmenityDTO getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Amenity not found with id: " + id));
        return mapToResponse(amenity);
    }

    public AmenityDTO createAmenity(AmenityDTO amenityDTO) {
        Amenity amenity = new Amenity();
        updateAmenityFromDTO(amenity, amenityDTO);
        Amenity savedAmenity = amenityRepository.save(amenity);
        return mapToResponse(savedAmenity);
    }

    public AmenityDTO updateAmenity(AmenityDTO amenityDTO) {
        Amenity existingAmenity = amenityRepository.findById(amenityDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Amenity not found with id: " + amenityDTO.getId()));

        updateAmenityFromDTO(existingAmenity, amenityDTO);
        Amenity updatedAmenity = amenityRepository.save(existingAmenity);
        return mapToResponse(updatedAmenity);
    }

    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Amenity not found with id: " + id);
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