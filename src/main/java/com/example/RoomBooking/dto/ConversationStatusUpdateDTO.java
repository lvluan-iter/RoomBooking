package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Message;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationStatusUpdateDTO {
    private String conversationId;
    private Long recipientId;
    private Message.MessageStatus status;

}