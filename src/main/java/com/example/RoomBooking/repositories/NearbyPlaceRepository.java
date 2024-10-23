package com.example.RoomBooking.repositories;

import com.example.RoomBooking.models.NearbyPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NearbyPlaceRepository extends JpaRepository<NearbyPlace, Long> {
    void deleteByPropertyId(Long propertyId);
}