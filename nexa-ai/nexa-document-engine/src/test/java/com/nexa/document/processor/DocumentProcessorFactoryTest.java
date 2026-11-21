package com.nexa.document.processor;

import com.nexa.document.DocumentProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentProcessorFactoryTest {

    @Test
    @DisplayName("Should route PDF files to PDFProcessor")
    void shouldRoutePdfToPdfProcessor() {
        DocumentProcessorFactory factory = new DocumentProcessorFactory();
        DocumentProcessor processor = factory.getProcessor("application/pdf", "annual_report.pdf");

        assertThat(processor).isInstanceOf(PDFProcessor.class);
    }

    @Test
    @DisplayName("Should route DOCX files to WordProcessor")
    void shouldRouteDocxToWordProcessor() {
        DocumentProcessorFactory factory = new DocumentProcessorFactory();
        DocumentProcessor processor = factory.getProcessor(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "document.docx");

        assertThat(processor).isInstanceOf(WordProcessor.class);
    }

    @Test
    @DisplayName("Should route XLSX files to ExcelProcessor")
    void shouldRouteXlsxToExcelProcessor() {
        DocumentProcessorFactory factory = new DocumentProcessorFactory();
        DocumentProcessor processor = factory.getProcessor(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "financials.xlsx");

        assertThat(processor).isInstanceOf(ExcelProcessor.class);
    }

    @Test
    @DisplayName("Should route TXT and MD files to TikaDocumentProcessor")
    void shouldRouteTxtAndMdToTikaProcessor() {
        DocumentProcessorFactory factory = new DocumentProcessorFactory();

        DocumentProcessor txtProcessor = factory.getProcessor("text/plain", "notes.txt");
        assertThat(txtProcessor).isInstanceOf(TikaDocumentProcessor.class);

        DocumentProcessor mdProcessor = factory.getProcessor("text/markdown", "readme.md");
        assertThat(mdProcessor).isInstanceOf(TikaDocumentProcessor.class);
    }

    @Test
    @DisplayName("Should fallback to TikaDocumentProcessor for unknown types")
    void shouldFallbackToTikaProcessor() {
        DocumentProcessorFactory factory = new DocumentProcessorFactory();
        DocumentProcessor processor = factory.getProcessor("unknown/type", "custom.data");

        assertThat(processor).isInstanceOf(TikaDocumentProcessor.class);
    }
}
