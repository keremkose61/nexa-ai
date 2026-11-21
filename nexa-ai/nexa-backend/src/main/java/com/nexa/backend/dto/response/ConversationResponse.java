package com.nexa.backend.dto.response;

import com.nexa.backend.entity.Conversation.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private UUID id;
    private String title;
    private ConversationStatus status;
    private boolean favorite;
    private boolean archived;
    private String aiModel;
    private int totalMessages;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
