package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Property;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullname;
    private String phoneNumber;
    private String gender;
    private Date birthdate;
    private String avatar;
    private Set<String> roles;
    private Set<Long> favoritePropertyIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isPublicProfile;
    private boolean isPublicPhone;
    private boolean isPublicEmail ;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<Long> getFavoritePropertyIds() {
        return favoritePropertyIds;
    }

    public void setFavoritePropertyIds(Set<Long> favoritePropertyIds) {
        this.favoritePropertyIds = favoritePropertyIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPublicProfile() {
        return isPublicProfile;
    }

    public void setPublicProfile(boolean publicProfile) {
        isPublicProfile = publicProfile;
    }

    public boolean isPublicPhone() {
        return isPublicPhone;
    }

    public void setPublicPhone(boolean publicPhone) {
        isPublicPhone = publicPhone;
    }

    public boolean isPublicEmail() {
        return isPublicEmail;
    }

    public void setPublicEmail(boolean publicEmail) {
        isPublicEmail = publicEmail;
    }
}
