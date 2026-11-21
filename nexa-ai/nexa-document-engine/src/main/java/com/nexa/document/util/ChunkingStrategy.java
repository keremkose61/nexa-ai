package com.nexa.document.util;

import com.nexa.document.model.DocumentChunk;

import java.util.ArrayList;
import java.util.List;

public class ChunkingStrategy {

    private final int chunkSize;
    private final int overlap;

    public ChunkingStrategy(int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("Overlap must be non-negative and less than chunk size");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public static ChunkingStrategy defaultStrategy() {
        return new ChunkingStrategy(1000, 200);
    }

    public List<DocumentChunk> chunk(String text) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int textLength = text.length();
        int chunkIndex = 0;
        int start = 0;

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);

            // Try to break at a paragraph or sentence boundary
            if (end < textLength) {
                int lastNewLine = text.lastIndexOf('\n', end);
                int lastPeriod = text.lastIndexOf(". ", end);
                int boundary = Math.max(lastNewLine, lastPeriod);
                if (boundary > start + (chunkSize / 2)) {
                    end = boundary + 1;
                }
            }

            String chunkContent = text.substring(start, end).trim();
            if (!chunkContent.isBlank()) {
                chunks.add(DocumentChunk.builder()
                        .chunkIndex(chunkIndex++)
                        .content(chunkContent)
                        .startChar(start)
                        .endChar(end)
                        .tokenEstimate((int) Math.ceil(chunkContent.length() / 4.0))
                        .build());
            }

            if (end >= textLength) break;
            int nextStart = end - overlap;
            start = Math.max(start + 1, nextStart);
        }

        return chunks;
    }
}
