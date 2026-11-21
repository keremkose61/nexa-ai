package com.nexa.desktop.service;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public ApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorCode = "UNKNOWN";
    }

    public ApiException(int statusCode, String message, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorCode = "UNKNOWN";
    }
}
