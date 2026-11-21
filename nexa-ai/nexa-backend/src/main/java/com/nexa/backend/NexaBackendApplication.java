package com.nexa.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.nexa.backend", "com.nexa.security"})
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class NexaBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexaBackendApplication.class, args);
    }
}
