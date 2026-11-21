package com.nexa.document.util;

import com.nexa.common.exception.NexaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"report.pdf", "doc.docx", "data.xlsx", "table.csv", "readme.txt", "notes.md", "payload.json"})
    @DisplayName("Should accept valid supported file extensions")
    void shouldAcceptValidExtensions(String fileName) {
        assertThatCode(() -> FileValidator.validateFile(fileName, 1024))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"virus.exe", "script.sh", "page.php", "archive.zip", "pic.jpg"})
    @DisplayName("Should reject unsupported file extensions")
    void shouldRejectUnsupportedExtensions(String fileName) {
        assertThatThrownBy(() -> FileValidator.validateFile(fileName, 1024))
                .isInstanceOf(NexaException.class);
    }

    @Test
    @DisplayName("Should reject path traversal attempts")
    void shouldRejectPathTraversal() {
        assertThatThrownBy(() -> FileValidator.validateFile("../secret.pdf", 1024))
                .isInstanceOf(NexaException.class);
        assertThatThrownBy(() -> FileValidator.validateFile("sub/file.pdf", 1024))
                .isInstanceOf(NexaException.class);
        assertThatThrownBy(() -> FileValidator.validateFile("sub\\file.pdf", 1024))
                .isInstanceOf(NexaException.class);
    }

    @Test
    @DisplayName("Should reject empty or null file name")
    void shouldRejectEmptyFileName() {
        assertThatThrownBy(() -> FileValidator.validateFile(null, 1024))
                .isInstanceOf(NexaException.class);
        assertThatThrownBy(() -> FileValidator.validateFile("   ", 1024))
                .isInstanceOf(NexaException.class);
    }

    @Test
    @DisplayName("Should reject file exceeding size limit")
    void shouldRejectOversizedFile() {
        long oversized = 51L * 1024 * 1024; // 51MB > 50MB
        assertThatThrownBy(() -> FileValidator.validateFile("large.pdf", oversized))
                .isInstanceOf(NexaException.class);
    }

    @Test
    @DisplayName("Should correctly extract file extension")
    void shouldExtractFileExtension() {
        assertThat(FileValidator.getFileExtension("test.pdf")).isEqualTo("pdf");
        assertThat(FileValidator.getFileExtension("archive.tar.gz")).isEqualTo("gz");
        assertThat(FileValidator.getFileExtension("noextension")).isEqualTo("");
        assertThat(FileValidator.getFileExtension(null)).isEqualTo("");
    }
}
