package com.nexa.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {
    private String fileName;
    private String contentType;
    private long size;
    private int pageCount;
    private int characterCount;
    private int wordCount;
    private Map<String, String> metadata;
    private LocalDateTime parsedAt;
}
