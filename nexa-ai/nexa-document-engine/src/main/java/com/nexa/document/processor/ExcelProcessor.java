package com.nexa.document.processor;

import com.nexa.document.DocumentProcessor;
import com.nexa.document.model.DocumentChunk;
import com.nexa.document.model.DocumentInfo;
import com.nexa.document.model.ProcessingResult;
import com.nexa.document.util.ChunkingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
public class ExcelProcessor implements DocumentProcessor {

    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final ChunkingStrategy chunkingStrategy = ChunkingStrategy.defaultStrategy();

    @Override
    public boolean supports(String contentType, String fileName) {
        if (contentType != null && (contentType.equalsIgnoreCase(CONTENT_TYPE)
                || contentType.equalsIgnoreCase("application/vnd.ms-excel")
                || contentType.contains("spreadsheetml")
                || contentType.contains("excel"))) {
            return true;
        }
        return fileName != null && (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"));
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
                 Workbook workbook = new XSSFWorkbook(is)) {
                return processWorkbook(workbook, fileName, fileSize);
            }
        } catch (Exception e) {
            log.error("Failed to process Excel file: {}", filePath, e);
            return ProcessingResult.failure("Excel processing failed: " + e.getMessage());
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
                 Workbook workbook = new XSSFWorkbook(bais)) {
                return processWorkbook(workbook, fileName, bytes.length);
            }
        } catch (Exception e) {
            log.error("Failed to process Excel stream for file: {}", fileName, e);
            return ProcessingResult.failure("Excel processing failed: " + e.getMessage());
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
                 Workbook workbook = new XSSFWorkbook(is)) {

                Map<String, String> metadata = extractExcelMetadata(workbook);
                int sheetCount = workbook.getNumberOfSheets();

                return DocumentInfo.builder()
                        .fileName(fileName)
                        .contentType(CONTENT_TYPE)
                        .size(fileSize)
                        .pageCount(sheetCount)
                        .characterCount(0)
                        .wordCount(0)
                        .metadata(metadata)
                        .parsedAt(LocalDateTime.now())
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to extract metadata from Excel file: {}", filePath, e);
            throw new RuntimeException("Failed to extract Excel metadata: " + e.getMessage(), e);
        }
    }

    private ProcessingResult processWorkbook(Workbook workbook, String fileName, long fileSize) {
        DataFormatter formatter = new DataFormatter();
        StringBuilder fullTextBuilder = new StringBuilder();
        int sheetCount = workbook.getNumberOfSheets();

        for (int i = 0; i < sheetCount; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            fullTextBuilder.append("--- Sheet: ").append(sheet.getSheetName()).append(" ---\n");

            for (Row row : sheet) {
                short lastCellNum = row.getLastCellNum();
                if (lastCellNum <= 0) {
                    continue;
                }

                List<String> cellValues = new ArrayList<>();
                boolean rowHasContent = false;

                for (int c = 0; c < lastCellNum; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String formattedValue = cell != null ? formatter.formatCellValue(cell).trim() : "";
                    if (!formattedValue.isEmpty()) {
                        rowHasContent = true;
                    }
                    cellValues.add(formattedValue);
                }

                if (rowHasContent) {
                    fullTextBuilder.append(String.join(" | ", cellValues)).append("\n");
                }
            }
            fullTextBuilder.append("\n");
        }

        String fullText = fullTextBuilder.toString().trim();
        int characterCount = fullText.length();
        int wordCount = calculateWordCount(fullText);

        Map<String, String> metadata = extractExcelMetadata(workbook);

        DocumentInfo documentInfo = DocumentInfo.builder()
                .fileName(fileName)
                .contentType(CONTENT_TYPE)
                .size(fileSize)
                .pageCount(sheetCount)
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

    private Map<String, String> extractExcelMetadata(Workbook workbook) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sheetCount", String.valueOf(workbook.getNumberOfSheets()));

        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            sheetNames.add(workbook.getSheetName(i));
        }
        metadata.put("sheetNames", String.join(", ", sheetNames));

        if (workbook instanceof XSSFWorkbook xssfWorkbook) {
            try {
                POIXMLProperties props = xssfWorkbook.getProperties();
                if (props != null && props.getCoreProperties() != null) {
                    POIXMLProperties.CoreProperties core = props.getCoreProperties();
                    if (core.getTitle() != null && !core.getTitle().isBlank()) {
                        metadata.put("title", core.getTitle());
                    }
                    if (core.getCreator() != null && !core.getCreator().isBlank()) {
                        metadata.put("creator", core.getCreator());
                    }
                    if (core.getDescription() != null && !core.getDescription().isBlank()) {
                        metadata.put("description", core.getDescription());
                    }
                    if (core.getKeywords() != null && !core.getKeywords().isBlank()) {
                        metadata.put("keywords", core.getKeywords());
                    }
                    if (core.getCategory() != null && !core.getCategory().isBlank()) {
                        metadata.put("category", core.getCategory());
                    }
                    if (core.getSubject() != null && !core.getSubject().isBlank()) {
                        metadata.put("subject", core.getSubject());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to extract full Excel workbook properties: {}", e.getMessage());
            }
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
