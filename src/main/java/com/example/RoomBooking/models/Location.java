package com.example.RoomBooking.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "location")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "location")
    @Builder.Default
    private List<Property> properties = new ArrayList<>();
}

