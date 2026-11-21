package com.nexa.desktop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDto {
    private UUID conversationId;
    private String content;
    private String aiModel;
    private Double temperature;
    private Integer maxTokens;
}
