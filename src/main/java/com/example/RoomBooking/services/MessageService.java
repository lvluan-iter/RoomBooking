package com.example.RoomBooking.services;

import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Message;
import com.example.RoomBooking.models.User;
import com.example.RoomBooking.repositories.MessageRepository;
import com.example.RoomBooking.dto.ConversationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public int getUnreadMessageCount(Long recipientId) {
        return messageRepository.countByRecipientIdAndStatus(recipientId, Message.MessageStatus.SENT);
    }

    public Message save(Message message) {
        if (message.getConversationId() == null) {
            message.setConversationId(generateConversationId(message.getSender(), message.getRecipient()));
        }
        message.setCreatedAt(LocalDateTime.now());
        message.setStatus(Message.MessageStatus.SENT);

        return messageRepository.save(message);
    }

    private String generateConversationId(User sender, User recipient) {
        if (sender == null || recipient == null || sender.getId() == null || recipient.getId() == null) {
            throw new IllegalArgumentException("Sender and recipient must not be null!");
        }
        long userId1 = Math.min(sender.getId(), recipient.getId());
        long userId2 = Math.max(sender.getId(), recipient.getId());
        return userId1 + "_" + userId2;
    }

    public List<Message> getConversation(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public Message updateMessageStatus(Long messageId, Message.MessageStatus newStatus) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " +messageId));
        message.setStatus(newStatus);
        return messageRepository.save(message);
    }

    public List<Message> updateConversationStatus(String conversationId, Long recipientId, Message.MessageStatus newStatus) {
        List<Message> messages = messageRepository.findByConversationIdAndRecipientIdAndStatusNot(
                conversationId, recipientId, newStatus);
        messages.forEach(message -> message.setStatus(newStatus));
        return messageRepository.saveAll(messages);
    }

    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    public Message updateMessage(Long messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " +messageId));
        message.setContent(newContent);
        return messageRepository.save(message);
    }

    public List<ConversationDTO> getUserConversations(Long userId) {
        List<Message> allUserMessages = messageRepository.findAllMessagesByUserId(userId);

        Map<String, List<Message>> conversationGroups = allUserMessages.stream()
                .collect(Collectors.groupingBy(Message::getConversationId));

        return conversationGroups.entrySet().stream()
                .map(entry -> convertToConversationDTO(entry.getKey(), entry.getValue(), userId))
                .sorted(Comparator.comparing(ConversationDTO::getLatestMessageTime).reversed())
                .collect(Collectors.toList());
    }

    private ConversationDTO convertToConversationDTO(String conversationId, List<Message> messages, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversationId);

        Message latestMessage = messages.stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found !"));

        dto.setLatestMessageContent(latestMessage.getContent());
        dto.setLatestMessageTime(latestMessage.getCreatedAt());

        Long otherUserId = latestMessage.getSender().getId().equals(currentUserId)
                ? latestMessage.getRecipient().getId()
                : latestMessage.getSender().getId();

        dto.setOtherUserId(otherUserId);
        dto.setOtherUserName(otherUserId.equals(latestMessage.getSender().getId())
                ? latestMessage.getSender().getFullname()
                : latestMessage.getRecipient().getFullname());

        long unreadCount = messages.stream()
                .filter(m -> m.getRecipient().getId().equals(currentUserId) && m.getStatus() != Message.MessageStatus.READ)
                .count();

        dto.setUnreadCount((int) unreadCount);

        return dto;
    }
}