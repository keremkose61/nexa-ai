package com.nexa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank(message = "Message content is required")
    @Size(max = 32000, message = "Message too long")
    private String content;
    private boolean stream;
    private String aiModel;
    private Double temperature;
    private Integer maxTokens;
}
