package com.nexa.backend.dto.response;

import com.nexa.backend.entity.Message.MessageRole;
import com.nexa.backend.entity.Message.MessageStatus;
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
public class MessageResponse {
    private UUID id;
    private MessageRole role;
    private String content;
    private MessageStatus status;
    private String aiModel;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private boolean edited;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
