package com.nexa.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelDto {
    private String id;
    private String name;
    private String provider;
    private String description;
    private int contextWindow;
    private boolean vision;
    private boolean reasoning;
}
