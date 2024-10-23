package com.example.RoomBooking.dto;

import java.util.List;

public class InitialDataDTO {
    private int unreadCount;
    private List<ConversationDTO> conversations;

    // Constructors, getters, and setters
    public InitialDataDTO(int unreadCount, List<ConversationDTO> conversations) {
        this.unreadCount = unreadCount;
        this.conversations = conversations;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<ConversationDTO> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationDTO> conversations) {
        this.conversations = conversations;
    }
}
