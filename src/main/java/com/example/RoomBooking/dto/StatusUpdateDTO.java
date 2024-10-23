package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Message;

public class StatusUpdateDTO {
    private Long messageId;
    private String conversationId;
    private Long senderId;
    private Long recipientId;
    private Message.MessageStatus status;

    // Constructors
    public StatusUpdateDTO() {}

    // Getters and setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
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
