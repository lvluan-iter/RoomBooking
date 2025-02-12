package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.CategoryRequest;
import com.example.RoomBooking.dto.CategoryResponse;
import com.example.RoomBooking.exceptions.ResourceAlreadyExistsException;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Category;
import com.example.RoomBooking.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void addCategory(CategoryRequest request) {
        boolean exists = categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName());
        if (exists) {
            throw new ResourceAlreadyExistsException("Category already exists !");
        }
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setImageUrl(request.getImageUrl());
        categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        categoryRepository.delete(category);
    }

    public void updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        boolean exists = categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName());
        if (exists && !category.getCategoryName().equalsIgnoreCase(request.getCategoryName())) {
            throw new ResourceAlreadyExistsException("Category already exists !");
        }

        category.setCategoryName(request.getCategoryName());
        category.setImageUrl(request.getImageUrl());
        categoryRepository.save(category);
    }


    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategoryName(category.getCategoryName());
        response.setImageUrl(category.getImageUrl());
        return response;
    }
}
