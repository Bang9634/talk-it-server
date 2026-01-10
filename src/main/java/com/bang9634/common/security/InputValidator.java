package com.bang9634.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Input validation utility to prevent XSS and injection attacks.
 */
public class InputValidator {
    private static final Logger logger = LoggerFactory.getLogger(InputValidator.class);
    
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int MIN_MESSAGE_LENGTH = 1;
    
    // Dangerous patterns for XSS detection
    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("</iframe>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("document\\.cookie", Pattern.CASE_INSENSITIVE),
        Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE)
    };
    
    /**
     * Validate a chat message content.
     */
    public static ValidationResult validateMessage(String content) {
        if (content == null) {
            return ValidationResult.error("null content");
        }
        
        String trimmed = content.trim();
        
        if (trimmed.isEmpty()) {
            return ValidationResult.error("empty content");
        }
        
        if (trimmed.length() < MIN_MESSAGE_LENGTH) {
            return ValidationResult.error("content too short (minimum " + MIN_MESSAGE_LENGTH + " characters)");
        }
        
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            return ValidationResult.error("message too long (maximum " + MAX_MESSAGE_LENGTH + " characters)");
        }
        
        // Check for dangerous patterns
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(content).find()) {
                logger.warn("Dangerous pattern detected: pattern={}, content={}", pattern.pattern(), 
                    content.substring(0, Math.min(50, content.length())));
                return ValidationResult.error("contains disallowed content");
            }
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Sanitize input string by escaping HTML special characters.
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }
    
    /**
     * Result of input validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    private InputValidator() {
        // Prevent instantiation
        throw new AssertionError("Cannot instantiate InputValidator");
    }
}