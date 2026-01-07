package com.bang9634.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.chat.model.ChatMessage;
import com.bang9634.common.util.IdGenerator;
import com.google.inject.Singleton;

/**
 * Service class for managing chat messages.
 * Handles creation, storage, retrieval, and validation of messages.
 */
@Singleton
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final List<ChatMessage> messageHistory;
    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * Constructor.
     */
    public MessageService() {
        this.messageHistory = new CopyOnWriteArrayList<>();
    }

    /**
     * Create and add a new chat message.
     * @param userId The ID of the user sending the message
     * @param username The username of the user sending the message
     * @param content The content of the message
     * @param type The type of the message
     * @return The created ChatMessage object
     */
    public ChatMessage createMessage( 
        String userId, String username, 
        String content, ChatMessage.MessageType type
    ) {
        ChatMessage message = new ChatMessage(userId, username, content, type);
        message.setMessageId(IdGenerator.generateMessageId());

        addMessage(message);
        return message;
    }

    /**
     * Add a message to the history.
     * @param message The ChatMessage to add
     */
    public void addMessage(ChatMessage message) {
        messageHistory.add(message);
        if (messageHistory.size() > MAX_HISTORY_SIZE) {
            // Remove oldest message to maintain size limit
            messageHistory.remove(0);
        }

        logger.debug("Added message: {} - {}", 
            message.getUsername(), message.getContent());
    }

    /**
     * Retrieve the message history.
     * @return List of ChatMessage objects
     */
    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    /**
     * Retrieve the most recent messages up to a specified count.
     * @param count The number of recent messages to retrieve
     * @return List of recent ChatMessage objects
     */
    public List<ChatMessage> getRecentMessages(int count) {
        int start = Math.max(0, messageHistory.size() - count);
        return new ArrayList<>(messageHistory.subList(start, messageHistory.size()));
    }

    /**
     * Get the count of messages in history.
     * @return The number of messages stored
     */
    public int getMessageCount() {
        return messageHistory.size();
    }

    /**
     * Validate message content.
     * @param content The content of the message
     * @return True if valid, false otherwise
     */
    public boolean validateMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        // limit message length to 500 characters
        if (content.length() > 500) {
            logger.warn("Message content too long: {} characters", content.length());
            return false;
        }
        return true;
    }

    /**
     * Clear the message history.
     */
    public void clearHistory() {
        messageHistory.clear();
        logger.info("Message history cleared.");
    }
}
