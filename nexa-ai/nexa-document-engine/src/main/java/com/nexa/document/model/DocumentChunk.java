package com.nexa.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {
    private int chunkIndex;
    private String content;
    private int startChar;
    private int endChar;
    private int pageNumber;
    private int tokenEstimate;
}
