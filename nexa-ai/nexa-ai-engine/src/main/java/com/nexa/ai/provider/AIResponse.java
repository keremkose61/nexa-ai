package com.nexa.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {
    private String id;
    private String content;
    private String model;
    private AIUsage usage;
    private String finishReason;
    private boolean error;
    private String errorMessage;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static AIResponse ofError(String errorMessage) {
        return AIResponse.builder()
                .error(true)
                .errorMessage(errorMessage)
                .build();
    }

    public static AIResponse ofContent(String content, String model) {
        return AIResponse.builder()
                .content(content)
                .model(model)
                .build();
    }
}
