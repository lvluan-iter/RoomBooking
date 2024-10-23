package com.example.RoomBooking.repositories;

import com.example.RoomBooking.models.TourRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TourRequestRepository extends JpaRepository<TourRequest, Long> {
    List<TourRequest> findByPropertyId(Long propertyId);
    List<TourRequest> findByStatus(TourRequest.TourStatus status);

    @Query("SELECT t FROM TourRequest t WHERE t.appointmentDate = :date AND t.status = 'confirmed'")
    List<TourRequest> findByAppointmentDateAndConfirm(@Param("date") LocalDate date);

    @Query("SELECT COUNT(t) FROM TourRequest t WHERE t.property.id = :propertyId " +
            "AND FUNCTION('DATE_FORMAT', t.createdAt, '%Y-%m') = :yearMonth")
    long countTourRequestsByPropertyIdAndYearMonth(@Param("propertyId") Long propertyId,
                                                   @Param("yearMonth") String yearMonth);

    List<TourRequest> findByEmail(String email);
}
