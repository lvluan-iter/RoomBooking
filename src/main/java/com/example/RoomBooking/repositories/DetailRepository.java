package com.example.RoomBooking.repositories;

import com.example.RoomBooking.models.Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface DetailRepository extends JpaRepository<Detail, Long> {
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Detail d WHERE d.detailDate >= :startDate AND d.detailDate < :endDate")
    BigDecimal calculateRevenueMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
