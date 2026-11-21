package com.nexa.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Auth
    INVALID_CREDENTIALS("Invalid email or password", 401),
    USER_NOT_FOUND("User not found", 404),
    USER_ALREADY_EXISTS("User with this email already exists", 409),
    ACCOUNT_DISABLED("Account is disabled", 403),
    TOKEN_EXPIRED("Authentication token has expired", 401),
    TOKEN_INVALID("Invalid authentication token", 401),
    REFRESH_TOKEN_EXPIRED("Refresh token has expired", 401),
    REFRESH_TOKEN_INVALID("Invalid refresh token", 401),
    ACCESS_DENIED("Access denied", 403),
    UNAUTHORIZED("Unauthorized", 401),

    // Conversation
    CONVERSATION_NOT_FOUND("Conversation not found", 404),
    MESSAGE_NOT_FOUND("Message not found", 404),

    // AI Provider
    AI_PROVIDER_NOT_FOUND("AI provider not found", 404),
    AI_PROVIDER_CONNECTION_FAILED("Failed to connect to AI provider", 503),
    AI_REQUEST_FAILED("AI request failed", 502),
    AI_STREAM_FAILED("AI streaming failed", 502),
    AI_RATE_LIMITED("AI provider rate limit exceeded", 429),

    // Document
    DOCUMENT_NOT_FOUND("Document not found", 404),
    DOCUMENT_PROCESSING_FAILED("Document processing failed", 500),
    FILE_TOO_LARGE("File size exceeds maximum allowed size", 413),
    UNSUPPORTED_FILE_TYPE("File type is not supported", 415),
    FILE_UPLOAD_FAILED("File upload failed", 500),
    FILE_NOT_FOUND("File not found", 404),

    // Project
    PROJECT_NOT_FOUND("Project not found", 404),

    // Settings
    SETTINGS_NOT_FOUND("Settings not found", 404),

    // General
    VALIDATION_ERROR("Validation error", 400),
    INTERNAL_ERROR("An internal error occurred", 500),
    RESOURCE_NOT_FOUND("Resource not found", 404),
    BAD_REQUEST("Bad request", 400);

    private final String message;
    private final int httpStatus;

    ErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
