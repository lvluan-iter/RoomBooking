package com.example.RoomBooking.dto;

import java.time.LocalDateTime;

public class ConversationDTO {
    private String conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String latestMessageContent;
    private LocalDateTime latestMessageTime;
    private int unreadCount;

    // Getters and setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getLatestMessageContent() {
        return latestMessageContent;
    }

    public void setLatestMessageContent(String latestMessageContent) {
        this.latestMessageContent = latestMessageContent;
    }

    public LocalDateTime getLatestMessageTime() {
        return latestMessageTime;
    }

    public void setLatestMessageTime(LocalDateTime latestMessageTime) {
        this.latestMessageTime = latestMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}