package com.example.RoomBooking.dto;

public class UnreadMessageNotification {
    private Long senderId;
    private int unreadCount;

    public UnreadMessageNotification(Long senderId, int unreadCount) {
        this.senderId = senderId;
        this.unreadCount = unreadCount;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
