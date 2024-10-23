package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.ConversationDTO;
import com.example.RoomBooking.dto.InitialDataDTO;
import com.example.RoomBooking.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class InitialDataController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/get-initial-data")
    @SendToUser("/queue/initial-data")
    public InitialDataDTO getInitialData(Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        int unreadCount = messageService.getUnreadMessageCount(userId);
        List<ConversationDTO> conversations = messageService.getUserConversations(userId);
        return new InitialDataDTO(unreadCount, conversations);
    }
}