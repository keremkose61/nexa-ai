package com.nexa.ai.exception;

import lombok.Getter;

@Getter
public class AIProviderException extends RuntimeException {
    public enum ErrorType {
        CONNECTION_FAILED, AUTHENTICATION_FAILED, RATE_LIMITED,
        TIMEOUT, INVALID_RESPONSE, STREAM_FAILED, MODEL_NOT_FOUND
    }

    private final ErrorType errorType;
    private final int httpStatusCode;

    public AIProviderException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
        this.httpStatusCode = 0;
    }

    public AIProviderException(String message, ErrorType errorType, int httpStatusCode) {
        super(message);
        this.errorType = errorType;
        this.httpStatusCode = httpStatusCode;
    }

    public AIProviderException(String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.httpStatusCode = 0;
    }
}
