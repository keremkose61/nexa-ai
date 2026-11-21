package com.nexa.ai.context;

import com.nexa.ai.provider.AIMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConversationContext {
    private final String conversationId;
    private final List<AIMessage> messages = new ArrayList<>();
    private int totalTokenEstimate = 0;
    private static final int MAX_TOKENS = 4096;
    private static final int ESTIMATE_CHARS_PER_TOKEN = 4;

    public ConversationContext(String conversationId) {
        this.conversationId = conversationId;
    }

    public void addMessage(AIMessage message) {
        messages.add(message);
        totalTokenEstimate += estimateTokens(message.getContent());
        trimIfNeeded();
    }

    private int estimateTokens(String text) {
        if (text == null) return 0;
        return (int) Math.ceil(text.length() / (double) ESTIMATE_CHARS_PER_TOKEN);
    }

    private void trimIfNeeded() {
        while (totalTokenEstimate > MAX_TOKENS && messages.size() > 2) {
            // Keep system message, remove oldest non-system message
            int removeIndex = messages.get(0).getRole() == AIMessage.Role.SYSTEM ? 1 : 0;
            AIMessage removed = messages.remove(removeIndex);
            totalTokenEstimate -= estimateTokens(removed.getContent());
        }
    }

    public List<AIMessage> getMessagesForRequest() {
        return new ArrayList<>(messages);
    }

    public void clear() {
        messages.clear();
        totalTokenEstimate = 0;
    }
}
