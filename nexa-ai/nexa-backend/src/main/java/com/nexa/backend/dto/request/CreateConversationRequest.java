package com.nexa.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateConversationRequest {
    @Size(max = 500)
    private String title;
    private UUID aiProviderId;
    private String aiModel;
    private String systemPrompt;
    private UUID projectId;
}
