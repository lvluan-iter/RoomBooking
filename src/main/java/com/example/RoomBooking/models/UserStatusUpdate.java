package com.example.RoomBooking.models;

import com.example.RoomBooking.models.UserStatus;

public class UserStatusUpdate {
    private Long userId;
    private UserStatus status;

    public UserStatusUpdate(Long userId, UserStatus status) {
        this.userId = userId;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}