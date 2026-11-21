package com.nexa.document;

import com.nexa.document.model.DocumentInfo;
import com.nexa.document.model.ProcessingResult;

import java.io.InputStream;
import java.nio.file.Path;

public interface DocumentProcessor {
    boolean supports(String contentType, String fileName);
    ProcessingResult process(Path filePath);
    ProcessingResult process(InputStream inputStream, String fileName);
    DocumentInfo extractMetadata(Path filePath);
}
