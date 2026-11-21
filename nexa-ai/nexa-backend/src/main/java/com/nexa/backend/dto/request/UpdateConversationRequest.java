package com.nexa.backend.dto.request;

import com.nexa.backend.entity.Conversation.ConversationStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateConversationRequest {
    private String title;
    private Boolean favorite;
    private Boolean archived;
    private ConversationStatus status;
    private String aiModel;
    private UUID aiProviderId;
    private String systemPrompt;
}
