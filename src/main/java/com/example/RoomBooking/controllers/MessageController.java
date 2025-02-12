package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.models.Message;
import com.example.RoomBooking.models.User;
import com.example.RoomBooking.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload MessageDTO messageDTO) {
        Message message = convertToEntity(messageDTO);
        Message savedMessage = messageService.save(message);
        MessageDTO savedMessageDTO = convertToDTO(savedMessage);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(savedMessageDTO.getRecipientId()),
                "/topic/messages",
                savedMessageDTO
        );

        int unreadCount = messageService.getUnreadMessageCount(message.getRecipient().getId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getRecipient().getId()),
                "/queue/unread",
                new UnreadMessageNotification(message.getSender().getId(), unreadCount)
        );

        updateConversations(message.getSender().getId(), message.getRecipient().getId());
    }

    @MessageMapping("/chat/message/status")
    public void updateMessageStatus(@Payload StatusUpdateDTO statusUpdateDTO) {
        Message updatedMessage = messageService.updateMessageStatus(statusUpdateDTO.getMessageId(), statusUpdateDTO.getStatus());
        MessageDTO updatedMessageDTO = convertToDTO(updatedMessage);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(updatedMessageDTO.getSenderId()),
                "/topic/message-status",
                updatedMessageDTO
        );
        messagingTemplate.convertAndSendToUser(
                String.valueOf(updatedMessageDTO.getRecipientId()),
                "/topic/message-status",
                updatedMessageDTO
        );

        updateConversations(updatedMessageDTO.getSenderId(), updatedMessageDTO.getRecipientId());
    }

    @MessageMapping("/chat/conversation/status")
    public void updateConversationStatus(@Payload ConversationStatusUpdateDTO statusUpdateDTO) {

        List<Message> updatedMessages = messageService.updateConversationStatus(
                statusUpdateDTO.getConversationId(),
                statusUpdateDTO.getRecipientId(),
                statusUpdateDTO.getStatus()
        );
        List<MessageDTO> updatedMessageDTOs = updatedMessages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(statusUpdateDTO.getRecipientId()),
                "/topic/conversation-status",
                updatedMessageDTOs
        );

        int unreadCount = messageService.getUnreadMessageCount(statusUpdateDTO.getRecipientId());
        messagingTemplate.convertAndSendToUser(
                String.valueOf(statusUpdateDTO.getRecipientId()),
                "/queue/unread",
                new UnreadMessageNotification(statusUpdateDTO.getRecipientId(), unreadCount)
        );

        updateConversations(statusUpdateDTO.getRecipientId(), statusUpdateDTO.getRecipientId());
    }

    private void updateConversations(Long senderId, Long recipientId) {
        List<ConversationDTO> senderConversations = messageService.getUserConversations(senderId);
        List<ConversationDTO> recipientConversations = messageService.getUserConversations(recipientId);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(senderId),
                "/queue/conversations",
                senderConversations
        );

        messagingTemplate.convertAndSendToUser(
                String.valueOf(recipientId),
                "/queue/conversations",
                recipientConversations
        );
    }

    @MessageMapping("/delete")
    public void deleteMessage(@Payload Long messageId) {
        messageService.deleteMessage(messageId);
        messagingTemplate.convertAndSend("/topic/message-deleted", messageId);
    }

    @MessageMapping("/edit")
    public void editMessage(@Payload MessageDTO messageDTO) {
        Message updatedMessage = messageService.updateMessage(messageDTO.getId(), messageDTO.getContent());
        MessageDTO updatedMessageDTO = convertToDTO(updatedMessage);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(updatedMessage.getSender().getId()),
                "/topic/message-edited",
                updatedMessageDTO
        );
        messagingTemplate.convertAndSendToUser(
                String.valueOf(updatedMessage.getRecipient().getId()),
                "/topic/message-edited",
                updatedMessageDTO
        );
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getConversation(@PathVariable String conversationId) {
        List<Message> messages = messageService.getConversation(conversationId);
        List<MessageDTO> messageDTOs = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messageDTOs);
    }

    private Message convertToEntity(MessageDTO messageDTO) {
        Message message = new Message();
        message.setId(messageDTO.getId());

        User sender = new User();
        sender.setId(messageDTO.getSenderId());
        message.setSender(sender);

        User recipient = new User();
        recipient.setId(messageDTO.getRecipientId());
        message.setRecipient(recipient);

        message.setContent(messageDTO.getContent());
        message.setCreatedAt(messageDTO.getCreatedAt());
        message.setStatus(messageDTO.getStatus());
        return message;
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setRecipientId(message.getRecipient().getId());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setStatus(message.getStatus());
        return dto;
    }
}