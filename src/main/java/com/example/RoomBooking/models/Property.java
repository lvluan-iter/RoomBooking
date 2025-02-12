package com.example.RoomBooking.models;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double price;

    @Column(name = "is_available", columnDefinition = "TINYINT(1) default 1")
    private boolean isAvailable;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private Integer bedrooms;

    @Column(nullable = false)
    private Integer bathrooms;

    @Column(nullable = false)
    private Double area;

    @ManyToMany
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_property_amenity",
                    columnNames = {"property_id", "amenity_id"}
            )
    )
    private List<Amenity> amenities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(name = "visits", columnDefinition = "Double default 0")
    private Double visits;

    @Column(name = "furniture")
    private String furniture;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NearbyPlace> nearbyPlaces = new ArrayList<>();

    @Column(name = "type")
    private String type;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "is_paid", columnDefinition = "TINYINT(1) default 0")
    private boolean isPaid;

    @Column(name = "is_locked", columnDefinition = "TINYINT(1) default 0")
    private boolean isLocked;

    @Column(name = "is_approved", columnDefinition = "TINYINT(1) default 0")
    private boolean isApproved;

    @Column(name = "reason")
    private String reason;

    @Column(name = "expiration_date")
    private Timestamp expirationDate;

    public void addImage(Image image) {
        images.add(image);
        image.setProperty(this);
    }

    public void removeImage(Image image) {
        images.remove(image);
        image.setProperty(null);
    }

    public void addNearbyPlace(NearbyPlace nearbyPlace) {
        nearbyPlaces.add(nearbyPlace);
        nearbyPlace.setProperty(this);
    }

    public void removeNearbyPlace(NearbyPlace nearbyPlace) {
        nearbyPlaces.remove(nearbyPlace);
        nearbyPlace.setProperty(null);
    }
}
