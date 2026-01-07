package com.bang9634.chat.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.chat.model.ChatMessage;
import com.bang9634.common.util.JsonUtil;
import com.bang9634.user.model.User;
import com.bang9634.user.service.UserSessionManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ChatRoomService {
    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);

    private final UserSessionManager userSessionManager;
    private final MessageService messageService;

    @Inject
    public ChatRoomService(UserSessionManager userSessionManager, MessageService messageService) {
        this.userSessionManager = userSessionManager;
        this.messageService = messageService;
        logger.info("ChatRoomService initialized.");
    }

    public void broadcastMessage(ChatMessage message) {
        String jsonMessage = JsonUtil.toJson(message);

        userSessionManager.getAllUsers().forEach(user -> {
            sendToUser(user, jsonMessage);
        });

        logger.info("Broadcast [{}]: {} - {}",
            message.getType(), message.getUsername(), message.getContent());
    }

    public void sendToUser(User user, String jsonMessage) {
        try {
            if (user.getSession() != null && user.getSession().isOpen()) {
                user.getSession().getRemote().sendString(jsonMessage);
            }
        } catch (IOException e) {
            logger.error("Failed to send message to user {}: {}",
                user.getUsername(), e.getMessage());
        }
    }

    public void handleUserJoin(User user) {
        ChatMessage joinMessage = messageService.createMessage(
            user.getUserId(),
            "SYSTEM",
            user.getDisplayName() + " has joined the chat.",
            ChatMessage.MessageType.JOIN
        );

        broadcastMessage(joinMessage);

        messageService.getRecentMessages(50).forEach(msg -> {
            String jsonMsg = JsonUtil.toJson(msg);
            sendToUser(user, jsonMsg);
        });
    }

    public void handleUserLeave(User user) {
        ChatMessage leaveMessage = messageService.createMessage(
            user.getUserId(),
            "SYSTEM",
            user.getDisplayName() + " has left the chat.",
            ChatMessage.MessageType.LEAVE
        );

        broadcastMessage(leaveMessage);
    }

    public void handleChatMessage(User sender, String content) {
        if (!messageService.validateMessage(content)) {
            logger.warn("Invalid message from user {}: {}",
                sender.getUsername(), content);
                return;
        }

        ChatMessage chatMessage = messageService.createMessage(
            sender.getUserId(),
            sender.getDisplayName(),
            content,
            ChatMessage.MessageType.CHAT
        );

        broadcastMessage(chatMessage);
    }

}
