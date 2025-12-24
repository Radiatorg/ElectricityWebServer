package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.ChatMessageRequest;
import com.verchuk.electro.dto.response.ChatMessageResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.ChatMessage;
import com.verchuk.electro.model.User;
import com.verchuk.electro.repository.ChatMessageRepository;
import com.verchuk.electro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Transactional
    public ChatMessageResponse createMessage(ChatMessageRequest request) {
        User sender = getCurrentUser();
        boolean isAdmin = sender.getRoles().stream()
                .anyMatch(r -> r.getName() == com.verchuk.electro.model.Role.RoleName.ADMIN);

        ChatMessage replyToMessage = null;
        if (request.getReplyToMessageId() != null) {
            replyToMessage = chatMessageRepository.findById(request.getReplyToMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", request.getReplyToMessageId()));
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .messageText(request.getMessageText())
                .imageUrl(request.getImageUrl())
                .deleted(false)
                .replyToMessage(replyToMessage)
                .build();

        // Если админ отвечает на сообщение, назначаем это сообщение ему (только если еще не назначен)
        if (isAdmin && replyToMessage != null && replyToMessage.getAssignedAdmin() == null) {
            replyToMessage.setAssignedAdmin(sender);
            chatMessageRepository.save(replyToMessage);
        }

        ChatMessage saved = chatMessageRepository.save(message);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));

        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == com.verchuk.electro.model.Role.RoleName.ADMIN);

        // Проверяем, что пользователь является отправителем или администратором
        if (message.getSender().getId().equals(currentUser.getId()) || isAdmin) {
            message.setDeleted(true);
            chatMessageRepository.save(message);
        } else {
            throw new RuntimeException("Нет прав на удаление сообщения");
        }
    }

    public List<ChatMessageResponse> getAllMessages() {
        return chatMessageRepository.findAllActiveMessages().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ChatMessageResponse getMessageById(Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", "id", messageId));
        return mapToResponse(message);
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        ChatMessageResponse.ChatMessageResponseBuilder builder = ChatMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderFirstName(message.getSender().getFirstName())
                .senderLastName(message.getSender().getLastName())
                .messageText(message.getMessageText())
                .imageUrl(message.getImageUrl())
                .createdAt(message.getCreatedAt())
                .deleted(message.getDeleted());

        if (message.getAssignedAdmin() != null) {
            builder.assignedAdminId(message.getAssignedAdmin().getId())
                    .assignedAdminUsername(message.getAssignedAdmin().getUsername())
                    .assignedAdminFirstName(message.getAssignedAdmin().getFirstName())
                    .assignedAdminLastName(message.getAssignedAdmin().getLastName());
        }

        if (message.getReplyToMessage() != null) {
            builder.replyToMessageId(message.getReplyToMessage().getId());
        }

        return builder.build();
    }
}

