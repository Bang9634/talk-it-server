package com.bang9634.common.exception;

public class MessageTooLongException extends BaseException {
    private final int messageLength;
    private final int maxLength;

    public MessageTooLongException(int messageLength, int maxLength) {
        super(ErrorCode.MESSAGE_TOO_LONG);
        this.messageLength = messageLength;
        this.maxLength = maxLength;
    }

    @Override
    public String getMessage() {
        return String.format("Message length %d exceeds maximum %d characters.",
            messageLength, maxLength);
    }
}
