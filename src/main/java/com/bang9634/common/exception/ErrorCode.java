package com.bang9634.common.exception;

/**
 * Standardized error codes for application exceptions.
 */
public enum ErrorCode {
    // ===== Authentication & Authorization =====
    INVALID_CREDENTIALS(401, "AUTH001", "Invalid username or password"),
    TOKEN_EXPIRED(401, "AUTH002", "Authentication token has expired"),
    TOKEN_INVALID(401, "AUTH003", "Invalid authentication token"),
    UNAUTHORIZED(401, "AUTH004", "Authentication required"),
    FORBIDDEN(403, "AUTH005", "Access denied"),
    
    // ===== User Management =====
    USER_NOT_FOUND(404, "USER001", "User not found"),
    USER_ALREADY_EXISTS(409, "USER002", "Username already exists"),
    INVALID_USERNAME(400, "USER003", "Invalid username format"),
    INVALID_PASSWORD(400, "USER004", "Password does not meet requirements"),
    INVALID_EMAIL(400, "USER005", "Invalid email format"),
    
    // ===== Chat & Messages =====
    ROOM_NOT_FOUND(404, "CHAT001", "Chat room not found"),
    ROOM_ALREADY_EXISTS(409, "CHAT002", "Chat room already exists"),
    MESSAGE_TOO_LONG(400, "CHAT003", "Message exceeds maximum length"),
    MESSAGE_TOO_SHORT(400, "CHAT004", "Message is too short"),
    INVALID_MESSAGE_FORMAT(400, "CHAT005", "Invalid message format"),
    
    // ===== Rate Limiting =====
    RATE_LIMIT_EXCEEDED(429, "RATE001", "Too many requests"),
    MESSAGE_RATE_LIMIT(429, "RATE002", "Too many messages sent"),
    CONNECTION_RATE_LIMIT(429, "RATE003", "Too many connection attempts"),
    
    // ===== System Errors =====
    DATABASE_ERROR(500, "SYS001", "Database error occurred"),
    INTERNAL_ERROR(500, "SYS002", "Internal server error"),
    SERVICE_UNAVAILABLE(503, "SYS003", "Service temporarily unavailable");
    
    private final int statusCode;
    private final String errorCode;
    private final String message;
    
    ErrorCode(int statusCode, String errorCode, String message) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getStatusCode() { return statusCode; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (HTTP %d)", errorCode, message, statusCode);
    }
}
