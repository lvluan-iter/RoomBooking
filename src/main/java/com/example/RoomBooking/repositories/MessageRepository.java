package com.example.RoomBooking.repositories;

import com.example.RoomBooking.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    List<Message> findByConversationIdAndRecipientIdAndStatusNot(
            String conversationId, Long recipientId, Message.MessageStatus status);

    int countByRecipientIdAndStatus(Long recipientId, Message.MessageStatus status);

    @Query("SELECT m FROM Message m WHERE m.id IN " +
            "(SELECT MAX(m2.id) FROM Message m2 WHERE m2.sender.id = :userId OR m2.recipient.id = :userId " +
            "GROUP BY m2.conversationId) " +
            "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessagesByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<Message> findAllMessagesByUserId(@Param("userId") Long userId);
}