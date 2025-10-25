package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.CategoryRequest;
import com.example.RoomBooking.dto.CategoryResponse;
import com.example.RoomBooking.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/")
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody CategoryRequest categoryRequest) {
        categoryService.addCategory(categoryRequest);
        return ResponseEntity.ok("Category added successfully.");
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted successfully.");
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryRequest categoryRequest) {
        categoryService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.ok("Category updated successfully.");
    }
}
