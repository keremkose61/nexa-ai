package com.nexa.backend.config;

import com.nexa.ai.context.ContextManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public ContextManager contextManager() {
        return new ContextManager();
    }
}
