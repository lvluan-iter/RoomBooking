package com.example.RoomBooking.specifications;

import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.models.Amenity;
import com.example.RoomBooking.dto.PropertySearchDTO;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PropertySpecifications {

    public static Specification<Property> createSpecification(PropertySearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("isAvailable")));

            predicates.add(criteriaBuilder.isFalse(root.get("isLocked")));

            predicates.add(criteriaBuilder.and(
                    criteriaBuilder.isNotNull(root.get("expirationDate")),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("expirationDate"), LocalDateTime.now())
            ));

            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
                String lowercaseKeyword = "%" + searchDTO.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), lowercaseKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), lowercaseKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), lowercaseKeyword)
                ));
            }

            if (searchDTO.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), searchDTO.getMinPrice()));
            }
            if (searchDTO.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), searchDTO.getMaxPrice()));
            }

            if (searchDTO.getLocation() != null && !searchDTO.getLocation().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), "%" + searchDTO.getLocation().toLowerCase() + "%"));
            }

            if (searchDTO.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), searchDTO.getCategoryId()));
            }

            if (searchDTO.getMinArea() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("area"), searchDTO.getMinArea()));
            }
            if (searchDTO.getMaxArea() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("area"), searchDTO.getMaxArea()));
            }

            if (searchDTO.getBedrooms() != null) {
                predicates.add(criteriaBuilder.equal(root.get("bedrooms"), searchDTO.getBedrooms()));
            }

            if (searchDTO.getBathrooms() != null) {
                predicates.add(criteriaBuilder.equal(root.get("bathrooms"), searchDTO.getBathrooms()));
            }

            if (searchDTO.getAmenities() != null && !searchDTO.getAmenities().isEmpty()) {
                Join<Property, Amenity> amenityJoin = root.join("amenities");
                predicates.add(amenityJoin.get("id").in(searchDTO.getAmenities()));
            }

            if (searchDTO.getType() != null && !searchDTO.getType().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("type"), searchDTO.getType()));
            }

            query.distinct(true);

            query.orderBy(criteriaBuilder.desc(root.get("isPaid")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}