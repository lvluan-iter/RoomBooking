package com.example.RoomBooking.repositories;

import com.example.RoomBooking.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Override
    List<Category> findAll();

    Boolean existsByCategoryNameIgnoreCase(String categoryName);
}
