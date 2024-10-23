package com.example.RoomBooking.dto;

public class PropertyStats {
    private Long propertyId;
    private Long views;
    private Long likes;
    private Long requests;

    public PropertyStats(Long propertyId, Long views, Long likes, Long requests) {
        this.propertyId = propertyId;
        this.views = views;
        this.likes = likes;
        this.requests = requests;
    }

    public PropertyStats() {
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getRequests() {
        return requests;
    }

    public void setRequests(Long requests) {
        this.requests = requests;
    }
}
