package com.nexa.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIUsage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}
