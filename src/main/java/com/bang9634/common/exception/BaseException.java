package com.bang9634.common.exception;

/**
 * Base exception class for application-specific exceptions.
 * Encapsulates an ErrorCode for standardized error handling.
 * @see ErrorCode
 */
public class BaseException extends RuntimeException {
    ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
}
