package com.example.RoomBooking.repositories;

import com.example.RoomBooking.models.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

    Page<Property> findByIsAvailableTrue(Pageable pageable);

    @Query("SELECT p FROM Property p WHERE " +
            "(:location IS NULL OR p.location.name = :location) " +
            "AND p.id != :propertyId")
    Page<Property> searchNearBy(@Param("location") String location,
                                @Param("propertyId") Long propertyId,
                                Pageable pageable);

    @Query(value = "SELECT * FROM properties p ORDER BY p.visits DESC LIMIT 10", nativeQuery = true)
    List<Property> findTop10PopularProperties();

    @Query("SELECT COUNT(p) FROM Property p WHERE p.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Property p WHERE p.user.id = :userId AND p.createdAt >= :startDate AND p.createdAt < :endDate")
    Long countByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.id FROM Property p WHERE p.user.id = :userId")
    List<Long> findPropertyIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Property p WHERE p.user.id = :userId")
    List<Property> findPropertyByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) FROM Property p WHERE p.createdAt >= :startDate AND p.createdAt < :endDate")
    Long countPropertyCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.id FROM Property p")
    List<Long> findAllPropertyIds();
}
