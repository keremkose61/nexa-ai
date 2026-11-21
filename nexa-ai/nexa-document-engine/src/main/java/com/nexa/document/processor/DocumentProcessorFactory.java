package com.nexa.document.processor;

import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import com.nexa.document.DocumentProcessor;
import com.nexa.document.model.DocumentInfo;
import com.nexa.document.model.ProcessingResult;
import com.nexa.document.util.FileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class DocumentProcessorFactory {

    private final List<DocumentProcessor> processors;
    private final DocumentProcessor fallbackProcessor;

    @Autowired
    public DocumentProcessorFactory(List<DocumentProcessor> processors) {
        this.processors = processors != null ? new ArrayList<>(processors) : new ArrayList<>();

        DocumentProcessor fallback = null;
        for (DocumentProcessor processor : this.processors) {
            if (processor instanceof TikaDocumentProcessor) {
                fallback = processor;
                break;
            }
        }

        if (fallback == null) {
            fallback = new TikaDocumentProcessor();
            this.processors.add(fallback);
        }
        this.fallbackProcessor = fallback;
    }

    public DocumentProcessorFactory() {
        this.processors = new ArrayList<>();
        this.processors.add(new PDFProcessor());
        this.processors.add(new WordProcessor());
        this.processors.add(new ExcelProcessor());
        this.fallbackProcessor = new TikaDocumentProcessor();
        this.processors.add(this.fallbackProcessor);
    }

    public DocumentProcessor getProcessor(String contentType, String fileName) {
        // Try specific processors first (excluding generic fallback)
        for (DocumentProcessor processor : processors) {
            if (processor != fallbackProcessor && processor.supports(contentType, fileName)) {
                log.debug("Selected processor {} for file: {}, contentType: {}",
                        processor.getClass().getSimpleName(), fileName, contentType);
                return processor;
            }
        }

        // Try fallback processor
        if (fallbackProcessor.supports(contentType, fileName)) {
            log.debug("Selected fallback processor {} for file: {}, contentType: {}",
                    fallbackProcessor.getClass().getSimpleName(), fileName, contentType);
            return fallbackProcessor;
        }

        // If still no direct match, return fallback processor for any remaining text types
        log.warn("No specific processor matched for file: {}, contentType: {}. Using fallback processor.",
                fileName, contentType);
        return fallbackProcessor;
    }

    public DocumentProcessor getProcessor(Path filePath) {
        if (filePath == null) {
            throw new NexaException(ErrorCode.BAD_REQUEST, "File path cannot be null");
        }

        String fileName = filePath.getFileName().toString();
        String contentType = null;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            log.debug("Could not probe content type for {}: {}", filePath, e.getMessage());
        }

        return getProcessor(contentType, fileName);
    }

    public ProcessingResult processDocument(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new NexaException(ErrorCode.FILE_NOT_FOUND, "Document file not found: " + filePath);
        }

        try {
            String fileName = filePath.getFileName().toString();
            long fileSize = Files.size(filePath);

            FileValidator.validateFile(fileName, fileSize);

            DocumentProcessor processor = getProcessor(filePath);
            return processor.process(filePath);
        } catch (NexaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", filePath, e.getMessage(), e);
            throw new NexaException(ErrorCode.DOCUMENT_PROCESSING_FAILED,
                    "Failed to process document: " + e.getMessage(), e);
        }
    }

    public ProcessingResult processDocument(InputStream inputStream, String fileName, String contentType) {
        if (inputStream == null) {
            throw new NexaException(ErrorCode.BAD_REQUEST, "Input stream cannot be null");
        }

        try {
            DocumentProcessor processor = getProcessor(contentType, fileName);
            return processor.process(inputStream, fileName);
        } catch (NexaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process document stream {}: {}", fileName, e.getMessage(), e);
            throw new NexaException(ErrorCode.DOCUMENT_PROCESSING_FAILED,
                    "Failed to process document stream: " + e.getMessage(), e);
        }
    }

    public DocumentInfo extractMetadata(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new NexaException(ErrorCode.FILE_NOT_FOUND, "Document file not found: " + filePath);
        }

        try {
            DocumentProcessor processor = getProcessor(filePath);
            return processor.extractMetadata(filePath);
        } catch (NexaException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract metadata from {}: {}", filePath, e.getMessage(), e);
            throw new NexaException(ErrorCode.DOCUMENT_PROCESSING_FAILED,
                    "Failed to extract document metadata: " + e.getMessage(), e);
        }
    }

    public List<DocumentProcessor> getProcessors() {
        return Collections.unmodifiableList(processors);
    }
}
