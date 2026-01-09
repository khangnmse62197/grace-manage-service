package com.grace.gracemanageservice.common.constant;

public class ErrorMessages {

    // User errors
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String INVALID_USER_DATA = "Invalid user data provided";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String FORBIDDEN = "Access denied";

    // Validation errors
    public static final String INVALID_EMAIL = "Invalid email format";
    public static final String INVALID_PASSWORD = "Password does not meet requirements";
    public static final String INVALID_INPUT = "Invalid input provided";

    // Database errors
    public static final String DATABASE_ERROR = "Database operation failed";
    public static final String CONSTRAINT_VIOLATION = "Data constraint violation";

    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

