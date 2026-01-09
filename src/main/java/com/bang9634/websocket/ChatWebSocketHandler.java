package com.bang9634.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.chat.model.ChatMessage;
import com.bang9634.chat.service.ChatRoomService;
import com.bang9634.common.config.ServiceInjector;
import com.bang9634.common.security.InputValidator;
import com.bang9634.common.security.RateLimiter;
import com.bang9634.common.util.IpUtil;
import com.bang9634.common.util.JsonUtil;
import com.bang9634.user.model.User;
import com.bang9634.user.service.IpBlockService;
import com.bang9634.user.service.UserSessionManager;

/**
 * WebSocket handler for managing chat connections and messages.
 * Handles user connections, disconnections, and message processing.
 */
@WebSocket
public class ChatWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final UserSessionManager userSessionManager;
    private final ChatRoomService chatRoomService;
    private final IpBlockService ipBlockService;
    private final RateLimiter rateLimiter;

    /**
     * Constructor.
     * Initializes services via dependency injection.
     */
    public ChatWebSocketHandler() {
        this.userSessionManager = ServiceInjector.getInstance(UserSessionManager.class);
        this.chatRoomService = ServiceInjector.getInstance(ChatRoomService.class);
        this.ipBlockService = ServiceInjector.getInstance(IpBlockService.class);
        this.rateLimiter = ServiceInjector.getInstance(RateLimiter.class);
    }

    /**
     * Handle new WebSocket connection.
     * @param session The WebSocket session
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        String ip = IpUtil.extractIpAddress(session);
        logger.info("New connection attempt from IP: {}", ip);

        // Rate limiting check
        if (!rateLimiter.allowConnection(ip)) {
            logger.warn("Rate limit exceeded for IP: {}", ip);
            closeSession(session, 1008, "Rate limit exceeded.");
            return;
        }

        // Check if IP is blocked
        if (ipBlockService.isBlocked(ip)) {
            logger.warn("Connection attempt from blocked IP: {}", ip);
            closeSession(session, 1008, "Your IP is blocked.");
            return;
        }

        logger.info("WebSocket Connected: {}", session.getRemoteAddress());

        // Add user session
        User user = userSessionManager.addSession(session);

        // If user is null, it means the session was rejected (e.g., blocked IP)
        if (user == null) {
            logger.warn("Failed to add user session for IP: {}", ip);
            closeSession(session, 1008, "Connection rejected.");
            return;
        }
        chatRoomService.handleUserJoin(user);

        logger.info("User connected: {} ({}), total users: {}",
            user.getDisplayName(), user.getUserId(), userSessionManager.getUserCount());
    }

    /**
     * Handle incoming WebSocket message.
     * @param session The WebSocket session
     * @param message The received message
     */
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        logger.debug("Received message from {}: {}",
            session.getRemoteAddress(), message);
        
        User sender = userSessionManager.getUserBySession(session);
        if (sender == null) {
            logger.warn("Received message from unknown session: {}", session.getRemoteAddress());
            return;
        }

        String ip = sender.getIpAddress();

        if (!rateLimiter.allowMessage(ip)) {
            logger.warn("Rate limit exceeded for IP: {}", ip);
            return;
        }

        try {
            ChatMessage chatMessage = JsonUtil.fromJson(message, ChatMessage.class);

            if (chatMessage == null || chatMessage.getContent() == null) {
                logger.warn("Invalid message format from user {}: {}",
                    sender.getUsername(), message);
                return;
            }

            String sanitizedContent = InputValidator.sanitizeHtml(chatMessage.getContent());

            chatRoomService.handleChatMessage(sender, sanitizedContent);
        } catch (Exception e) {
            logger.error("Error processing message from user {}: {}",
                sender.getUsername(), e.getMessage());
        }
    }

    /**
     * Handle WebSocket disconnection.
     * @param session The WebSocket session
     * @param statusCode The status code
     * @param reason The reason for disconnection
     */
    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        logger.info("WebSocket Closed: {} (code: {}, reason: {})",
            session.getRemoteAddress(), statusCode, reason);

        User user = userSessionManager.getUserBySession(session);
        if (user != null) {
            chatRoomService.handleUserLeave(user);
            userSessionManager.removeSession(session);

            logger.info("User disconnected: {} ({}), total users: {}",
                user.getUsername(), user.getUserId(), userSessionManager.getUserCount());
        }
    }

    /**
     * Handle WebSocket error.
     * @param session The WebSocket session
     * @param error The error encountered
     */
    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket Error on session {}: {}",
            session.getRemoteAddress(), error);
        User user = userSessionManager.getUserBySession(session);
        if (user != null) {
            userSessionManager.removeSession(session);
        }
    }

    /**
     * Close a WebSocket session with a given status code and reason.
     * 
     * @param session The WebSocket session to close
     * @param statusCode The status code for closure
     * @param reason The reason for closure
     */
    private void closeSession(Session session, int statusCode, String reason) {
        try {
            if (session != null && session.isOpen()) {
                session.close(statusCode, reason);
            }
        } catch (Exception e) {
            logger.error("Error closing session {}: {}", reason, e);
        }
    }
}
