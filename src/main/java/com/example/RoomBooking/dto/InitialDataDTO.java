package com.example.RoomBooking.dto;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitialDataDTO {
    private int unreadCount;
    private List<ConversationDTO> conversations;

}
