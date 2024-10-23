package com.example.RoomBooking.dto;

import java.sql.Date;
import java.util.Set;

public class UserRequest {

    private String username;
    private String email;
    private String password;
    private String fullname;
    private String phoneNumber;
    private String gender;
    private Date birthdate;
    private String avatar;
    private Set<Long> roleIds;
    private Set<Long> favoritePropertyIds;
    private boolean isPublicProfile;
    private boolean isPublicPhone;
    private boolean isPublicEmail;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Long> getFavoritePropertyIds() {
        return favoritePropertyIds;
    }

    public void setFavoritePropertyIds(Set<Long> favoritePropertyIds) {
        this.favoritePropertyIds = favoritePropertyIds;
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
