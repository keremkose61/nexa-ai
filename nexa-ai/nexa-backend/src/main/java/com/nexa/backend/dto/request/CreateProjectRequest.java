package com.nexa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 10000)
    private String systemInstructions;

    private String emoji;
    private String color;
}
