package com.nexa.ai.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {
    private String model;
    private List<MessageDto> messages;
    private double temperature;
    @JsonProperty("max_tokens")
    private int maxTokens;
    private boolean stream;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDto {
        private String role;
        private String content;
    }
}
