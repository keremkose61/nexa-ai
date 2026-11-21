package com.nexa.ai.context;

import com.nexa.ai.provider.AIMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContextManager {
    private final Map<String, ConversationContext> contexts = new ConcurrentHashMap<>();

    public ConversationContext getOrCreate(String conversationId) {
        return contexts.computeIfAbsent(conversationId, ConversationContext::new);
    }

    public void addMessage(String conversationId, AIMessage message) {
        getOrCreate(conversationId).addMessage(message);
    }

    public void clearContext(String conversationId) {
        ConversationContext ctx = contexts.get(conversationId);
        if (ctx != null) ctx.clear();
    }

    public void removeContext(String conversationId) {
        contexts.remove(conversationId);
    }

    public boolean hasContext(String conversationId) {
        return contexts.containsKey(conversationId);
    }
}
