package com.nexa.document.util;

import com.nexa.document.model.DocumentChunk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChunkingStrategyTest {

    @Test
    @DisplayName("Should return empty list for null or blank input")
    void shouldReturnEmptyForNullOrBlank() {
        ChunkingStrategy strategy = ChunkingStrategy.defaultStrategy();
        assertThat(strategy.chunk(null)).isEmpty();
        assertThat(strategy.chunk("")).isEmpty();
        assertThat(strategy.chunk("   \n\t  ")).isEmpty();
    }

    @Test
    @DisplayName("Should create single chunk for short text")
    void shouldCreateSingleChunkForShortText() {
        ChunkingStrategy strategy = ChunkingStrategy.defaultStrategy();
        String text = "NEXA AI is a professional intelligence platform.";
        List<DocumentChunk> chunks = strategy.chunk(text);

        assertThat(chunks).hasSize(1);
        DocumentChunk chunk = chunks.get(0);
        assertThat(chunk.getChunkIndex()).isEqualTo(0);
        assertThat(chunk.getContent()).isEqualTo(text);
        assertThat(chunk.getTokenEstimate()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should split long text into overlapping chunks")
    void shouldSplitLongTextIntoOverlappingChunks() {
        ChunkingStrategy strategy = new ChunkingStrategy(50, 10);
        String text = "Paragraph one with some text to fill up the initial chunk size. " +
                "Paragraph two continues the discussion with further insights and details. " +
                "Paragraph three wraps up the document nicely.";

        List<DocumentChunk> chunks = strategy.chunk(text);

        assertThat(chunks).hasSizeGreaterThan(1);
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).getChunkIndex()).isEqualTo(i);
            assertThat(chunks.get(i).getContent()).isNotBlank();
        }
    }

    @Test
    @DisplayName("Should reject invalid chunkSize or overlap")
    void shouldRejectInvalidArguments() {
        assertThatThrownBy(() -> new ChunkingStrategy(0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChunkingStrategy(100, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChunkingStrategy(100, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
