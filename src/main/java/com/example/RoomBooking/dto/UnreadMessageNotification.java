package com.example.RoomBooking.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadMessageNotification {
    private Long senderId;
    private int unreadCount;
}
