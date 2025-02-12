package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Message;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateDTO {
    private Long messageId;
    private String conversationId;
    private Long senderId;
    private Long recipientId;
    private Message.MessageStatus status;

}
