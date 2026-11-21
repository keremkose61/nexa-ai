package com.nexa.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {
    private boolean success;
    private String fullText;
    private DocumentInfo documentInfo;
    private List<DocumentChunk> chunks;
    private String summary;
    private String errorMessage;

    public static ProcessingResult failure(String errorMessage) {
        return ProcessingResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
