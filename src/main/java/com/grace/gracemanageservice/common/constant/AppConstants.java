package com.grace.gracemanageservice.common.constant;


public class AppConstants {

    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    // Success/Error codes

    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_HEADER = "Authorization";
    // Security

    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MIN_PASSWORD_LENGTH = 8;
    // Validation

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_PAGE = 0;
    // Pagination defaults
}


