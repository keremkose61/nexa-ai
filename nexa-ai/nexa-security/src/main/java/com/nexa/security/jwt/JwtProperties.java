package com.nexa.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nexa.jwt")
public class JwtProperties {
    private String secret = "nexaai-default-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private long accessTokenExpirationMs = 900000; // 15 minutes
    private long refreshTokenExpirationMs = 604800000; // 7 days
    private String issuer = "nexa-ai";
}
