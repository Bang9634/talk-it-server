package com.bang9634.chat.model;

import java.time.LocalDateTime;

/**
 * Model class representing a chat message.
 * Includes message ID, user ID, username, content, timestamp, and message type.
 */
public class ChatMessage {
    private String messageId;
    private String userId;
    private String username;
    private String content;
    private LocalDateTime timestamp;
    private MessageType type;

    /**
     * Enum representing the type of chat message.
     * <ul>
     *  <li>JOIN: User joined the chat</li>
     *  <li>CHAT: Regular chat message</li>
     *  <li>LEAVE: User left the chat</li>
     *  <li>SYSTEM: System message</li>
     * </ul>
     * 
     */
    public enum MessageType {
        JOIN,   // User joined the chat
        CHAT,   // Regular chat message
        LEAVE,  // User left the chat
        SYSTEM  // System message
    }

    /**
     * Default constructor initializing timestamp to current time.
     */
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Parameterized constructor.
     * @param userId User ID of the message sender
     * @param username Username of the message sender
     * @param content Content of the message
     * @param type Type of the message
     */
    public ChatMessage(String userId, String username, String content, MessageType type) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp;}
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", timestamp=" + timestamp +
                '}';
    }
}
