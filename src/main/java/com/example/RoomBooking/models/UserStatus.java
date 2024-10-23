package com.example.RoomBooking.models;

public class UserStatus {
    private String status;
    private Long lastUpdated;

    public UserStatus(String status, Long lastUpdated) {
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}