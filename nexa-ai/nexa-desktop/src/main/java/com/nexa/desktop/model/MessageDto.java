package com.nexa.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageDto {
    private UUID id;
    private String role; // "USER", "ASSISTANT", "SYSTEM"
    private String content;
    private String status; // "SENT", "DELIVERED", "FAILED"
    private String aiModel;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private boolean edited;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
