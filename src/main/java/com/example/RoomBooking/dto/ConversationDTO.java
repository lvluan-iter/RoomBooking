package com.example.RoomBooking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {
    private String conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String latestMessageContent;
    private LocalDateTime latestMessageTime;
    private int unreadCount;

}