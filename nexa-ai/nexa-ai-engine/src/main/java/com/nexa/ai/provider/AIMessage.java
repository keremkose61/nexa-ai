package com.nexa.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIMessage {
    public enum Role {
        SYSTEM, USER, ASSISTANT
    }

    private Role role;
    private String content;

    public static AIMessage system(String content) {
        return new AIMessage(Role.SYSTEM, content);
    }

    public static AIMessage user(String content) {
        return new AIMessage(Role.USER, content);
    }

    public static AIMessage assistant(String content) {
        return new AIMessage(Role.ASSISTANT, content);
    }

    public String getRoleString() {
        return role.name().toLowerCase();
    }
}
