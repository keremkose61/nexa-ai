package com.nexa.document.util;

import com.nexa.common.constants.AppConstants;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;

import java.util.Set;

public final class FileValidator {
    private FileValidator() {}

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "docx", "xlsx", "csv", "txt", "md", "json"
    );

    public static void validateFile(String fileName, long fileSize) {
        if (fileName == null || fileName.isBlank()) {
            throw new NexaException(ErrorCode.BAD_REQUEST, "File name cannot be empty");
        }

        // Prevent path traversal
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new NexaException(ErrorCode.BAD_REQUEST, "Invalid file name");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new NexaException(ErrorCode.UNSUPPORTED_FILE_TYPE,
                    "Unsupported file format: ." + extension + ". Supported formats: " + ALLOWED_EXTENSIONS);
        }

        if (fileSize > AppConstants.MAX_FILE_SIZE_BYTES) {
            throw new NexaException(ErrorCode.FILE_TOO_LARGE,
                    "File size (" + (fileSize / (1024 * 1024)) + " MB) exceeds limit of " + AppConstants.MAX_FILE_SIZE_MB + " MB");
        }
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
