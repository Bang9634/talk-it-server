package com.bang9634.websocket.service;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.chat.model.ChatMessage;
import com.bang9634.chat.model.MessageRequest;
import com.bang9634.chat.model.UserListResponse;
import com.bang9634.chat.service.ChatRoomService;
import com.bang9634.common.security.InputValidator;
import com.bang9634.common.security.RateLimiter;
import com.bang9634.common.util.IpUtil;
import com.bang9634.common.util.JsonUtil;
import com.bang9634.user.model.User;
import com.bang9634.user.service.IpBlockService;
import com.bang9634.user.service.UserSessionManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Service for managing WebSocket connections and coordinating business logic.
 * Handles connection validation, message processing, and disconnection logic.
 */
@Singleton
public class WebSocketConnectionService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionService.class);
    private final UserSessionManager userSessionManager;
    private final ChatRoomService chatRoomService;
    private final IpBlockService ipBlockService;
    private final RateLimiter rateLimiter;

    @Inject
    public WebSocketConnectionService(
        UserSessionManager userSessionManager,
        ChatRoomService chatRoomService,
        IpBlockService ipBlockService,
        RateLimiter rateLimiter
    ) {
        this.userSessionManager = userSessionManager;
        this.chatRoomService = chatRoomService;
        this.ipBlockService = ipBlockService;
        this.rateLimiter = rateLimiter;
        logger.info("WebSocketConnectionService intialized.");
    }

    public ConnectionResult handleConnection(Session session) {
        String ip = IpUtil.extractIpAddress(session);
        logger.info("New connection attempt from IP: {}", ip);

        if (!rateLimiter.allowConnection(ip)) {
            logger.warn("Rate limit exceeded for IP: {}", ip);
            return ConnectionResult.failure(1008, "Rate limit exceeded.");
        }

        if (ipBlockService.isBlocked(ip)) {
            logger.warn("Connection attempt from blocked IP: {}", ip);
            return ConnectionResult.failure(1008, "Your IP is blocked.");
        }

        // TODO: need to implement authentication for registered users
        User user = userSessionManager.addSessionAnonymousUser(session);

        if (user == null) {
            logger.warn("Failed to add user session for IP: {}", ip);
            return ConnectionResult.failure(1008, "Connection rejected.");
        }

        chatRoomService.handleUserJoin(user);

        logger.info("User connected: {} ({}), total users: {}",
            user.getDisplayName(), user.getUserId(), userSessionManager.getUserCount());
        
        return ConnectionResult.success(user);
    }

    public MessageResult handleMessage(Session session, String message) {
        logger.debug("Received message from {}: {}", session.getRemoteAddress(), message);
        
        User sender = userSessionManager.getUserBySession(session);
        if (sender == null) {
            logger.warn("Received message from unknown session: {}", session.getRemoteAddress());
            return MessageResult.unknownSession();
        }

        String ip = sender.getIpAddress();
        if (!rateLimiter.allowMessage(ip)) {
            logger.warn("Rate limit exceeded for IP: {}", ip);
            return MessageResult.rateLimitExceeded();
        }

        try {
            MessageRequest messageRequest = JsonUtil.fromJson(message, MessageRequest.class);

            if (messageRequest == null) {
                logger.warn("Failed to parse message request from {}", session.getRemoteAddress());
                return MessageResult.invalidFormat();
            }

            if (messageRequest.isUserListRequest()) {
                logger.info("User list requested by: {}", sender.getUsername());
                return MessageResult.userListRequest();
            }

            ChatMessage chatMessage = JsonUtil.fromJson(message, ChatMessage.class);

            if (chatMessage == null || chatMessage.getContent() == null) {
                logger.warn("Invalid message format from user {}: {}",
                    sender.getUsername(), message);
                return MessageResult.invalidFormat();
            }

            String sanitizedContent = InputValidator.sanitizeHtml(chatMessage.getContent());
            chatRoomService.handleChatMessage(sender, sanitizedContent);

            return MessageResult.success();
        } catch (Exception e) {
            logger.error("Error processing message from user {}: {}",
                sender.getUsername(), e.getMessage());
            return MessageResult.error("Internal server error.");
        }
    }

    public void handleDisconnection(Session session) {
        User user = userSessionManager.getUserBySession(session);
        if (user != null) {
            chatRoomService.handleUserLeave(user);
            userSessionManager.removeSession(session);
            logger.info("User disconnected: {} ({}), total users: {}",
                user.getDisplayName(), user.getUserId(), userSessionManager.getUserCount());
        }
    }

    public UserListResponse getUserListResponse() {
        return userSessionManager.getUserListResponse();
    }

    public void broadcastUserList(String jsonResponse) {
        userSessionManager.getAllUsers().forEach(user -> {
            try {
                if (user.getSession() != null && user.getSession().isOpen()) {
                    user.getSession().getRemote().sendString(jsonResponse);
                }
            } catch (IOException e) {
                logger.error("Failed to broadcast user list to user: {}", user.getUsername());
            }
        });
    }

    public static class ConnectionResult {
        private final boolean success;
        private final int statusCode;
        private final String message;
        private final User user;

        private ConnectionResult(boolean success, int statusCode, String message, User user) {
            this.success = success;
            this.statusCode = statusCode;
            this.message = message;
            this.user = user;
        }

        public static ConnectionResult success(User user) {
            return new ConnectionResult(true, 1000, "Connected successfully.", user);
        }

        public static ConnectionResult failure(int statusCode, String message) {
            return new ConnectionResult(false, statusCode, message, null);
        }

        public boolean isSuccess() { return success; }
        public int getStatusCode() { return statusCode; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    public static class MessageResult {
        private final boolean success;
        private final MessageResultType type;
        private final String errorMessage;

        private MessageResult(boolean success, MessageResultType type, String errorMessage) {
            this.success = success;
            this.type = type;
            this.errorMessage = errorMessage;
        }

        public static MessageResult success() {
            return new MessageResult(true, MessageResultType.SUCCESS, null);
        }

        public static MessageResult unknownSession() {
            return new MessageResult(false, MessageResultType.UNKNOWN_SESSION, "Unknown session.");
        }

        public static MessageResult rateLimitExceeded() {
            return new MessageResult(false, MessageResultType.RATE_LIMIT_EXCEEDED, "Rate limit exceeded.");
        }

        public static MessageResult invalidFormat() {
            return new MessageResult(false, MessageResultType.INVALID_FORMAT, "Invalid message format.");
        }

        public static MessageResult userListRequest() {
            return new MessageResult(true, MessageResultType.USER_LIST_REQUEST, null);
        }

        public static MessageResult error(String errorMessage) {
            return new MessageResult(false, MessageResultType.ERROR, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public MessageResultType getType() { return type; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isUserListRequest() { return type == MessageResultType.USER_LIST_REQUEST; }  
    }

    public enum MessageResultType {
        SUCCESS,
        UNKNOWN_SESSION,
        RATE_LIMIT_EXCEEDED,
        INVALID_FORMAT,
        USER_LIST_REQUEST,
        ERROR
    }
}
