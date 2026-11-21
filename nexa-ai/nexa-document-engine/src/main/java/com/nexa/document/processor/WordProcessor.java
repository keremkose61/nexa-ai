package com.nexa.document.processor;

import com.nexa.document.DocumentProcessor;
import com.nexa.document.model.DocumentChunk;
import com.nexa.document.model.DocumentInfo;
import com.nexa.document.model.ProcessingResult;
import com.nexa.document.util.ChunkingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WordProcessor implements DocumentProcessor {

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private final ChunkingStrategy chunkingStrategy = ChunkingStrategy.defaultStrategy();

    @Override
    public boolean supports(String contentType, String fileName) {
        if (contentType != null && (contentType.equalsIgnoreCase(CONTENT_TYPE)
                || contentType.equalsIgnoreCase("application/msword")
                || contentType.contains("wordprocessingml"))) {
            return true;
        }
        return fileName != null && (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".doc"));
    }

    @Override
    public ProcessingResult process(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return ProcessingResult.failure("File does not exist: " + filePath);
        }

        try {
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();

            try (InputStream is = Files.newInputStream(filePath);
                 XWPFDocument document = new XWPFDocument(is)) {
                return processDocument(document, fileName, fileSize);
            }
        } catch (Exception e) {
            log.error("Failed to process Word file: {}", filePath, e);
            return ProcessingResult.failure("Word document processing failed: " + e.getMessage());
        }
    }

    @Override
    public ProcessingResult process(InputStream inputStream, String fileName) {
        if (inputStream == null) {
            return ProcessingResult.failure("Input stream cannot be null");
        }

        try {
            byte[] bytes = inputStream.readAllBytes();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                 XWPFDocument document = new XWPFDocument(bais)) {
                return processDocument(document, fileName, bytes.length);
            }
        } catch (Exception e) {
            log.error("Failed to process Word stream for file: {}", fileName, e);
            return ProcessingResult.failure("Word document processing failed: " + e.getMessage());
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

            try (InputStream is = Files.newInputStream(filePath);
                 XWPFDocument document = new XWPFDocument(is)) {

                Map<String, String> metadata = extractWordMetadata(document);
                int pageCount = extractPageCount(document, 0);

                return DocumentInfo.builder()
                        .fileName(fileName)
                        .contentType(CONTENT_TYPE)
                        .size(fileSize)
                        .pageCount(pageCount)
                        .characterCount(0)
                        .wordCount(0)
                        .metadata(metadata)
                        .parsedAt(LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to extract metadata from Word file: {}", filePath, e);
            throw new RuntimeException("Failed to extract Word metadata: " + e.getMessage(), e);
        }
    }

    private ProcessingResult processDocument(XWPFDocument document, String fileName, long fileSize) {
        StringBuilder fullTextBuilder = new StringBuilder();

        // Extract paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText();
            if (text != null && !text.isBlank()) {
                fullTextBuilder.append(text).append("\n");
            }
        }

        // Extract tables
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<String> cellTexts = new ArrayList<>();
                for (XWPFTableCell cell : row.getTableCells()) {
                    String cellText = cell.getText();
                    cellTexts.add(cellText != null ? cellText.trim() : "");
                }
                fullTextBuilder.append(String.join("\t", cellTexts)).append("\n");
            }
            fullTextBuilder.append("\n");
        }

        String fullText = fullTextBuilder.toString();
        int characterCount = fullText.length();
        int wordCount = calculateWordCount(fullText);
        int pageCount = extractPageCount(document, characterCount);

        Map<String, String> metadata = extractWordMetadata(document);

        DocumentInfo documentInfo = DocumentInfo.builder()
                .fileName(fileName)
                .contentType(CONTENT_TYPE)
                .size(fileSize)
                .pageCount(pageCount)
                .characterCount(characterCount)
                .wordCount(wordCount)
                .metadata(metadata)
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

    private Map<String, String> extractWordMetadata(XWPFDocument document) {
        Map<String, String> metadata = new HashMap<>();
        try {
            POIXMLProperties props = document.getProperties();
            if (props != null) {
                POIXMLProperties.CoreProperties coreProps = props.getCoreProperties();
                if (coreProps != null) {
                    if (coreProps.getTitle() != null && !coreProps.getTitle().isBlank()) {
                        metadata.put("title", coreProps.getTitle());
                    }
                    if (coreProps.getCreator() != null && !coreProps.getCreator().isBlank()) {
                        metadata.put("creator", coreProps.getCreator());
                    }
                    if (coreProps.getDescription() != null && !coreProps.getDescription().isBlank()) {
                        metadata.put("description", coreProps.getDescription());
                    }
                    if (coreProps.getKeywords() != null && !coreProps.getKeywords().isBlank()) {
                        metadata.put("keywords", coreProps.getKeywords());
                    }
                    if (coreProps.getCategory() != null && !coreProps.getCategory().isBlank()) {
                        metadata.put("category", coreProps.getCategory());
                    }
                    if (coreProps.getSubject() != null && !coreProps.getSubject().isBlank()) {
                        metadata.put("subject", coreProps.getSubject());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract full Word document properties: {}", e.getMessage());
        }
        return metadata;
    }

    private int extractPageCount(XWPFDocument document, int characterCount) {
        try {
            POIXMLProperties props = document.getProperties();
            if (props != null && props.getExtendedProperties() != null) {
                var underlying = props.getExtendedProperties().getUnderlyingProperties();
                if (underlying != null && underlying.getPages() > 0) {
                    return underlying.getPages();
                }
            }
        } catch (Exception e) {
            log.debug("Extended properties page count not available: {}", e.getMessage());
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
