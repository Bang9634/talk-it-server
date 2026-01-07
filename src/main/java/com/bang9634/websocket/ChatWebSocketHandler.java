package com.bang9634.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.chat.model.ChatMessage;
import com.bang9634.chat.service.ChatRoomService;
import com.bang9634.common.config.ServiceInjector;
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

    /**
     * Constructor.
     * Initializes services via dependency injection.
     */
    public ChatWebSocketHandler() {
        this.userSessionManager = ServiceInjector.getInstance(UserSessionManager.class);
        this.chatRoomService = ServiceInjector.getInstance(ChatRoomService.class);
        this.ipBlockService = ServiceInjector.getInstance(IpBlockService.class);
    }

    /**
     * Handle new WebSocket connection.
     * @param session The WebSocket session
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        String ip = IpUtil.extractIpAddress(session);
        logger.info("New connection attempt from IP: {}", ip);

        if (ipBlockService.isBlocked(ip)) {
            logger.warn("Connection attempt from blocked IP: {}", ip);
            try {
                session.close(1008, "Your IP is blocked.");
            } catch (Exception e) {
                logger.error("Error closing blocked session: {}", e.getMessage());
            }
            return;
        }



        logger.info("WebSocket Connected: {}", session.getRemoteAddress());

        User user = userSessionManager.addSession(session);
        chatRoomService.handleUserJoin(user);

        logger.info("User connected: {} ({}), total users: {}",
            user.getUsername(), user.getUserId(), userSessionManager.getUserCount());
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

        try {
            ChatMessage chatMessage = JsonUtil.fromJson(message, ChatMessage.class);

            if (chatMessage == null || chatMessage.getContent() == null) {
                logger.warn("Invalid message format from user {}: {}",
                    sender.getUsername(), message);
                return;
            }

            chatRoomService.handleChatMessage(sender, chatMessage.getContent());
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
}
