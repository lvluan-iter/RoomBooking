package com.example.RoomBooking.models;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullname;

    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "token_creation_date")
    private LocalDateTime tokenCreationDate;

    @Column(name = "gender")
    private String gender;

    @Column(name = "birthdate")
    private Date birthdate;

    @Column(name = "avatar")
    private String avatar;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "property_id")
    )
    private Set<Property> favoriteProperties = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_public_profile", columnDefinition = "TINYINT(1) default 0")
    private boolean isPublicProfile;

    @Column(name = "is_public_phone", columnDefinition = "TINYINT(1) default 0")
    private boolean isPublicPhone;

    @Column(name = "is_public_email", columnDefinition = "TINYINT(1) default 0")
    private boolean isPublicEmail;

    @Column(name = "is_locked", columnDefinition = "TINYINT(1) default 0")
    private boolean isLocked;

    @Column(name = "lock_reason")
    private String lockReason;

    @Column(name = "lock_expiration_date")
    private LocalDateTime lockExpirationDate;
}
