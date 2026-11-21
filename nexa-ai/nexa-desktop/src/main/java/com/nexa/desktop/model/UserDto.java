package com.nexa.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
