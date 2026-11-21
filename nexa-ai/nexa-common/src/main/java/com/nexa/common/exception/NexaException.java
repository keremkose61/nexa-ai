package com.nexa.common.exception;

import lombok.Getter;

@Getter
public class NexaException extends RuntimeException {
    private final ErrorCode errorCode;
    private final int httpStatus;

    public NexaException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public NexaException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public NexaException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }

    public NexaException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
    }
}
