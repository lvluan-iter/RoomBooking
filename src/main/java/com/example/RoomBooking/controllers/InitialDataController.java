package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.ConversationDTO;
import com.example.RoomBooking.dto.InitialDataDTO;
import com.example.RoomBooking.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class InitialDataController {
    private final MessageService messageService;

    @MessageMapping("/get-initial-data")
    @SendToUser("/queue/initial-data")
    public InitialDataDTO getInitialData(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        int unreadCount = messageService.getUnreadMessageCount(userId);
        List<ConversationDTO> conversations = messageService.getUserConversations(userId);
        return new InitialDataDTO(unreadCount, conversations);
    }
}