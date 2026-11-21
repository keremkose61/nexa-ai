package com.nexa.common.constants;

public final class AppConstants {
    private AppConstants() {}

    // Application
    public static final String APP_NAME = "NEXA AI";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_SLOGAN = "Your Personal AI Intelligence Platform";

    // API
    public static final String API_VERSION = "/api/v1";
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // File
    public static final long MAX_FILE_SIZE_MB = 50;
    public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    public static final String[] SUPPORTED_FILE_TYPES = {
            "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain", "text/csv", "text/markdown", "application/json"
    };

    // AI Context
    public static final int DEFAULT_MAX_CONTEXT_TOKENS = 4096;
    public static final int DEFAULT_CHUNK_SIZE = 1000;
    public static final int DEFAULT_CHUNK_OVERLAP = 200;

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Date formats
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
}
