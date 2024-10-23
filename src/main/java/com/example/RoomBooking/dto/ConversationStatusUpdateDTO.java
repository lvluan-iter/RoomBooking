package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Message;

public class ConversationStatusUpdateDTO {
    private String conversationId;
    private Long recipientId;
    private Message.MessageStatus status;

    // Constructors
    public ConversationStatusUpdateDTO() {}

    public ConversationStatusUpdateDTO(String conversationId, Long recipientId, Message.MessageStatus status) {
        this.conversationId = conversationId;
        this.recipientId = recipientId;
        this.status = status;
    }

    // Getters and setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Message.MessageStatus getStatus() {
        return status;
    }

    public void setStatus(Message.MessageStatus status) {
        this.status = status;
    }
}