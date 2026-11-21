package com.nexa.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private List<AIMessage> messages;
    private String model;
    @Builder.Default
    private double temperature = 0.7;
    @Builder.Default
    private int maxTokens = 2048;
    @Builder.Default
    private boolean stream = false;
    private String systemPrompt;

    public AIRequest withSystemPrompt(String systemPrompt) {
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            var allMessages = new java.util.ArrayList<AIMessage>();
            allMessages.add(AIMessage.system(systemPrompt));
            if (messages != null) allMessages.addAll(messages);
            return AIRequest.builder()
                    .messages(allMessages)
                    .model(this.model)
                    .temperature(this.temperature)
                    .maxTokens(this.maxTokens)
                    .stream(this.stream)
                    .build();
        }
        return this;
    }
}
