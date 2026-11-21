package com.nexa.document.processor;

import com.nexa.document.DocumentProcessor;
import com.nexa.document.model.DocumentChunk;
import com.nexa.document.model.DocumentInfo;
import com.nexa.document.model.ProcessingResult;
import com.nexa.document.util.ChunkingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PDFProcessor implements DocumentProcessor {

    private static final String CONTENT_TYPE = "application/pdf";
    private final ChunkingStrategy chunkingStrategy = ChunkingStrategy.defaultStrategy();

    @Override
    public boolean supports(String contentType, String fileName) {
        if (contentType != null && (contentType.equalsIgnoreCase(CONTENT_TYPE) || contentType.contains("pdf"))) {
            return true;
        }
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }

    @Override
    public ProcessingResult process(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            return ProcessingResult.failure("File does not exist: " + filePath);
        }

        try {
            File file = filePath.toFile();
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();

            try (PDDocument document = Loader.loadPDF(file)) {
                return processDocument(document, fileName, fileSize);
            }
        } catch (Exception e) {
            log.error("Failed to process PDF file: {}", filePath, e);
            return ProcessingResult.failure("PDF processing failed: " + e.getMessage());
        }
    }

    @Override
    public ProcessingResult process(InputStream inputStream, String fileName) {
        if (inputStream == null) {
            return ProcessingResult.failure("Input stream cannot be null");
        }

        try {
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                return processDocument(document, fileName, bytes.length);
            }
        } catch (Exception e) {
            log.error("Failed to process PDF stream for file: {}", fileName, e);
            return ProcessingResult.failure("PDF processing failed: " + e.getMessage());
        }
    }

    @Override
    public DocumentInfo extractMetadata(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        try {
            File file = filePath.toFile();
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();

            try (PDDocument document = Loader.loadPDF(file)) {
                PDDocumentInformation info = document.getDocumentInformation();
                int pageCount = document.getNumberOfPages();
                Map<String, String> metadata = extractPdfMetadata(info);

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
            log.error("Failed to extract metadata from PDF file: {}", filePath, e);
            throw new RuntimeException("Failed to extract PDF metadata: " + e.getMessage(), e);
        }
    }

    private ProcessingResult processDocument(PDDocument document, String fileName, long fileSize) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        String fullText = stripper.getText(document);
        if (fullText == null) {
            fullText = "";
        }

        int pageCount = document.getNumberOfPages();
        PDDocumentInformation info = document.getDocumentInformation();
        Map<String, String> metadata = extractPdfMetadata(info);

        int characterCount = fullText.length();
        int wordCount = calculateWordCount(fullText);

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

    private Map<String, String> extractPdfMetadata(PDDocumentInformation info) {
        Map<String, String> metadata = new HashMap<>();
        if (info == null) {
            return metadata;
        }

        if (info.getTitle() != null && !info.getTitle().isBlank()) {
            metadata.put("title", info.getTitle());
        }
        if (info.getAuthor() != null && !info.getAuthor().isBlank()) {
            metadata.put("author", info.getAuthor());
        }
        if (info.getSubject() != null && !info.getSubject().isBlank()) {
            metadata.put("subject", info.getSubject());
        }
        if (info.getKeywords() != null && !info.getKeywords().isBlank()) {
            metadata.put("keywords", info.getKeywords());
        }
        if (info.getCreator() != null && !info.getCreator().isBlank()) {
            metadata.put("creator", info.getCreator());
        }
        if (info.getProducer() != null && !info.getProducer().isBlank()) {
            metadata.put("producer", info.getProducer());
        }
        if (info.getCreationDate() != null) {
            metadata.put("creationDate", info.getCreationDate().getTime().toString());
        }
        if (info.getModificationDate() != null) {
            metadata.put("modificationDate", info.getModificationDate().getTime().toString());
        }

        return metadata;
    }

    private int calculateWordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
