package com.example.RoomBooking.dto;

import com.example.RoomBooking.models.Message;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime createdAt;
    private Message.MessageStatus status;

}