package com.nexa.backend.service;

import com.nexa.backend.dto.response.MessageResponse;
import com.nexa.backend.entity.Conversation;
import com.nexa.backend.entity.Message;
import com.nexa.backend.repository.ConversationRepository;
import com.nexa.backend.repository.MessageRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID conversationId, UUID userId, Pageable pageable) {
        // Verify conversation belongs to user
        conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public MessageResponse saveUserMessage(UUID conversationId, UUID userId, String content) {
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));

        Message message = Message.builder()
                .conversation(conversation)
                .role(Message.MessageRole.USER)
                .content(content)
                .status(Message.MessageStatus.COMPLETED)
                .build();

        message = messageRepository.save(message);

        conversation.setTotalMessages(conversation.getTotalMessages() + 1);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return mapToResponse(message);
    }

    @Transactional
    public MessageResponse saveAssistantMessage(UUID conversationId, String content, String model,
                                               Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NexaException(ErrorCode.CONVERSATION_NOT_FOUND));

        Message message = Message.builder()
                .conversation(conversation)
                .role(Message.MessageRole.ASSISTANT)
                .content(content)
                .aiModel(model != null ? model : conversation.getAiModel())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .status(Message.MessageStatus.COMPLETED)
                .build();

        message = messageRepository.save(message);

        conversation.setTotalMessages(conversation.getTotalMessages() + 1);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return mapToResponse(message);
    }

    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NexaException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getConversation().getUser().getId().equals(userId)) {
            throw new NexaException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        Conversation conversation = message.getConversation();
        messageRepository.delete(message);

        conversation.setTotalMessages(Math.max(0, conversation.getTotalMessages() - 1));
        conversationRepository.save(conversation);
    }

    public MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .status(message.getStatus())
                .aiModel(message.getAiModel())
                .promptTokens(message.getPromptTokens())
                .completionTokens(message.getCompletionTokens())
                .totalTokens(message.getTotalTokens())
                .edited(message.isEdited())
                .errorMessage(message.getErrorMessage())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
