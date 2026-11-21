package com.nexa.document.processor;

import com.nexa.document.DocumentProcessor;
import com.nexa.document.model.DocumentChunk;
import com.nexa.document.model.DocumentInfo;
import com.nexa.document.model.ProcessingResult;
import com.nexa.document.util.ChunkingStrategy;
import com.nexa.document.util.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class TikaDocumentProcessor implements DocumentProcessor {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "txt", "md", "markdown", "csv", "json", "log", "xml", "html", "htm", "yaml", "yml", "properties", "sql"
    );

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "text/plain", "text/markdown", "text/csv", "application/json", "application/xml",
            "text/xml", "text/html", "application/xhtml+xml", "text/yaml", "application/x-yaml"
    );

    private final Tika tika = new Tika();
    private final ChunkingStrategy chunkingStrategy = ChunkingStrategy.defaultStrategy();

    @Override
    public boolean supports(String contentType, String fileName) {
        if (contentType != null) {
            String lowerType = contentType.toLowerCase();
            if (lowerType.startsWith("text/") || SUPPORTED_CONTENT_TYPES.contains(lowerType)) {
                return true;
            }
        }

        if (fileName != null) {
            String ext = FileValidator.getFileExtension(fileName).toLowerCase();
            return SUPPORTED_EXTENSIONS.contains(ext);
        }

        return false;
    }

    @Override
    public ProcessingResult process(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return ProcessingResult.failure("File does not exist: " + filePath);
        }

        try {
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();
            Metadata metadata = new Metadata();

            String fullText;
            String detectedContentType;
            try (InputStream is = Files.newInputStream(filePath)) {
                detectedContentType = tika.detect(filePath);
                fullText = tika.parseToString(is, metadata);
            }

            return buildProcessingResult(fullText, metadata, fileName, detectedContentType, fileSize);
        } catch (Exception e) {
            log.error("Failed to process document with Tika: {}", filePath, e);
            return ProcessingResult.failure("Document processing failed: " + e.getMessage());
        }
    }

    @Override
    public ProcessingResult process(InputStream inputStream, String fileName) {
        if (inputStream == null) {
            return ProcessingResult.failure("Input stream cannot be null");
        }

        try {
            byte[] bytes = inputStream.readAllBytes();
            Metadata metadata = new Metadata();
            if (fileName != null) {
                metadata.set("resourceName", fileName);
            }

            String detectedContentType;
            String fullText;
            try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
                detectedContentType = tika.detect(is, fileName != null ? fileName : "unknown");
            }
            try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
                fullText = tika.parseToString(is, metadata);
            }

            return buildProcessingResult(fullText, metadata, fileName != null ? fileName : "stream-document",
                    detectedContentType, bytes.length);
        } catch (Exception e) {
            log.error("Failed to process document stream with Tika: {}", fileName, e);
            return ProcessingResult.failure("Document stream processing failed: " + e.getMessage());
        }
    }

    @Override
    public DocumentInfo extractMetadata(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        try {
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();
            Metadata metadata = new Metadata();

            String detectedContentType;
            try (InputStream is = Files.newInputStream(filePath)) {
                detectedContentType = tika.detect(filePath);
                tika.parseToString(is, metadata);
            }

            Map<String, String> metaMap = extractTikaMetadata(metadata);
            int pageCount = extractPageCount(metadata, 0);

            return DocumentInfo.builder()
                    .fileName(fileName)
                    .contentType(detectedContentType != null ? detectedContentType : "application/octet-stream")
                    .size(fileSize)
                    .pageCount(pageCount)
                    .characterCount(0)
                    .wordCount(0)
                    .metadata(metaMap)
                    .parsedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract metadata with Tika: {}", filePath, e);
            throw new RuntimeException("Failed to extract metadata: " + e.getMessage(), e);
        }
    }

    private ProcessingResult buildProcessingResult(String fullText, Metadata metadata, String fileName,
                                                   String detectedContentType, long fileSize) {
        if (fullText == null) {
            fullText = "";
        }

        Map<String, String> metaMap = extractTikaMetadata(metadata);
        int characterCount = fullText.length();
        int wordCount = calculateWordCount(fullText);
        int pageCount = extractPageCount(metadata, characterCount);

        DocumentInfo documentInfo = DocumentInfo.builder()
                .fileName(fileName)
                .contentType(detectedContentType != null ? detectedContentType : "text/plain")
                .size(fileSize)
                .pageCount(pageCount)
                .characterCount(characterCount)
                .wordCount(wordCount)
                .metadata(metaMap)
                .parsedAt(LocalDateTime.now())
                .build();

        List<DocumentChunk> chunks = chunkingStrategy.chunk(fullText);

        return ProcessingResult.builder()
                .success(true)
                .fullText(fullText)
                .documentInfo(documentInfo)
                .chunks(chunks)
                .build();
    }

    private Map<String, String> extractTikaMetadata(Metadata metadata) {
        Map<String, String> map = new HashMap<>();
        if (metadata == null) {
            return map;
        }

        for (String name : metadata.names()) {
            String value = metadata.get(name);
            if (value != null && !value.isBlank()) {
                map.put(name, value);
            }
        }
        return map;
    }

    private int extractPageCount(Metadata metadata, int characterCount) {
        if (metadata != null) {
            String pages = metadata.get("xmpTPg:NPages");
            if (pages == null) {
                pages = metadata.get("Page-Count");
            }
            if (pages == null) {
                pages = metadata.get("meta:page-count");
            }
            if (pages != null) {
                try {
                    int p = Integer.parseInt(pages.trim());
                    if (p > 0) return p;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return characterCount > 0 ? Math.max(1, (int) Math.ceil(characterCount / 3000.0)) : 1;
    }

    private int calculateWordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
