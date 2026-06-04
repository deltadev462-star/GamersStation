package com.thegamersstation.marketplace.messaging.service;

import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.common.util.ContentSanitizer;
import com.thegamersstation.marketplace.common.util.ProfanityFilter;
import com.thegamersstation.marketplace.messaging.dto.*;
import com.thegamersstation.marketplace.messaging.entity.Conversation;
import com.thegamersstation.marketplace.messaging.entity.ConversationParticipantStatus;
import com.thegamersstation.marketplace.messaging.entity.Message;
import com.thegamersstation.marketplace.common.util.StringUtil;
import com.thegamersstation.marketplace.messaging.mapper.MessageMapper;
import com.thegamersstation.marketplace.messaging.repository.ConversationParticipantStatusRepository;
import com.thegamersstation.marketplace.messaging.repository.ConversationRepository;
import com.thegamersstation.marketplace.messaging.repository.MessageRepository;
import com.thegamersstation.marketplace.notification.EmailNotificationService;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantStatusRepository participantStatusRepository;
    private final UsersRepository userRepository;
    private final MessageMapper messageMapper;
    private final ContentSanitizer contentSanitizer;
    private final ProfanityFilter profanityFilter;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailNotificationService emailNotificationService;
    
    private static final int MAX_MESSAGE_LENGTH = 5000;
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    public MessageDto sendMessage(Long conversationId, SendMessageRequest request, Long senderId) {
        // Validate conversation and sender
        Conversation conversation = conversationRepository
            .findByIdAndParticipantId(conversationId, senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if sender is blocked
        Long recipientId = conversation.getSeller().getId().equals(senderId) 
            ? conversation.getBuyer().getId() 
            : conversation.getSeller().getId();
            
        ConversationParticipantStatus recipientStatus = participantStatusRepository
            .findByConversationIdAndUserId(conversationId, recipientId)
            .orElse(null);
            
        if (recipientStatus != null && recipientStatus.getIsBlocked()) {
            throw new BusinessRuleException("Cannot send message to blocked user");
        }
        
        // Validate and sanitize content
        String sanitizedContent = sanitizeMessageContent(request.getContent());
        
        // Create message
        Message message = Message.builder()
            .conversation(conversation)
            .sender(sender)
            .content(sanitizedContent)
            .messageType(Message.MessageType.TEXT)
            .build();
        
        message = messageRepository.save(message);
        
        // Update conversation last message directly via repository
        conversationRepository.updateLastMessage(
            conversationId,
            message.getCreatedAt(),
            StringUtil.truncatePreview(sanitizedContent)
        );
        
        // Convert to DTO
        MessageDto messageDto = messageMapper.toDto(message, senderId);
        
        // Broadcast to recipient via WebSocket
        broadcastMessage(conversationId, recipientId, messageDto);
        
        // Send email notification to recipient (async, non-blocking)
        User recipient = userRepository.findById(recipientId)
            .orElse(null);
        if (recipient != null) {
            emailNotificationService.sendNewMessageNotification(
                recipient, sender, StringUtil.truncatePreview(sanitizedContent), conversationId);
        }
        
        log.info("Message sent in conversation {} by user {}", conversationId, senderId);
        
        return messageDto;
    }
    
    @Transactional(readOnly = true)
    public MessagesPageDto getMessages(Long conversationId, Long userId, Long cursor, Integer size) {
        // Validate user is participant
        conversationRepository.findByIdAndParticipantId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        // Use default size if not provided
        int pageSize = size != null ? Math.min(size, 100) : DEFAULT_PAGE_SIZE;
        
        // Fetch messages with cursor-based pagination
        Page<Message> messagePage = messageRepository.findByConversationIdAndUserId(
            conversationId,
            userId,
            cursor,
            PageRequest.of(0, pageSize)
        );
        
        // Convert to DTOs
        List<MessageDto> messageDtos = messagePage.getContent().stream()
            .map(message -> messageMapper.toDto(message, userId))
            .collect(Collectors.toList());
        
        // Reverse order for chronological display
        Collections.reverse(messageDtos);
        
        // Determine next cursor
        Long nextCursor = messagePage.hasNext() && !messagePage.getContent().isEmpty()
            ? messagePage.getContent().get(messagePage.getContent().size() - 1).getId()
            : null;
        
        return MessagesPageDto.builder()
            .messages(messageDtos)
            .totalMessages(messagePage.getTotalElements())
            .hasMore(messagePage.hasNext())
            .nextCursor(nextCursor)
            .count(messageDtos.size())
            .build();
    }
    
    public void markMessagesAsRead(Long conversationId, Long userId) {
        // Validate user is participant
        conversationRepository.findByIdAndParticipantId(conversationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        int updatedCount = messageRepository.markMessagesAsRead(
            conversationId,
            userId,
            LocalDateTime.now()
        );
        
        if (updatedCount > 0) {
            log.info("Marked {} messages as read in conversation {} for user {}", 
                updatedCount, conversationId, userId);
            
            // Notify sender via WebSocket that messages were read
            broadcastReadReceipts(conversationId, userId);
        }
    }
    
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findByIdAndParticipantId(messageId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        messageRepository.softDeleteMessage(messageId, userId);
        
        log.info("Message {} soft deleted by user {}", messageId, userId);
    }
    
    @Transactional(readOnly = true)
    public MessageDto getMessage(Long messageId, Long userId) {
        Message message = messageRepository.findByIdAndParticipantId(messageId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        return messageMapper.toDto(message, userId);
    }
    
    private String sanitizeMessageContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessRuleException("Message content cannot be empty");
        }
        
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessRuleException("Message content exceeds maximum length");
        }
        
        // Sanitize HTML/XSS
        String sanitized = contentSanitizer.sanitize(content);
        
        // Filter profanity
        sanitized = profanityFilter.filter(sanitized);
        
        return sanitized;
    }
    
    private void broadcastMessage(Long conversationId, Long recipientId, MessageDto messageDto) {
        try {
            // Send to specific user's queue (recipient)
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                messageDto
            );

            // Also broadcast to the conversation topic so all active participants update instantly
            messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId + ".messages",
                messageDto
            );

            log.debug("Broadcast message to user {} and topic for conversation {}", recipientId, conversationId);
        } catch (Exception e) {
            // Log error but don't fail the message send
            log.error("Failed to broadcast message via WebSocket", e);
        }
    }
    
    private void broadcastReadReceipts(Long conversationId, Long readByUserId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElse(null);
                
            if (conversation != null) {
                Long otherUserId = conversation.getSeller().getId().equals(readByUserId)
                    ? conversation.getBuyer().getId()
                    : conversation.getSeller().getId();
                
                // Create read receipt notification
                ReadReceiptDto readReceipt = ReadReceiptDto.builder()
                    .conversationId(conversationId)
                    .readByUserId(readByUserId)
                    .readAt(LocalDateTime.now())
                    .build();
                
                // Send to other user
                messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(),
                    "/queue/read-receipts",
                    readReceipt
                );
                
                log.debug("Broadcast read receipt to user {} for conversation {}", otherUserId, conversationId);
            }
        } catch (Exception e) {
            log.error("Failed to broadcast read receipt via WebSocket", e);
        }
    }
    
    
    // DTO for read receipts
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    static class ReadReceiptDto {
        private Long conversationId;
        private Long readByUserId;
        private LocalDateTime readAt;
    }
}