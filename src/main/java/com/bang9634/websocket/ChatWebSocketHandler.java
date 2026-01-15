package com.bang9634.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.chat.model.UserListResponse;
import com.bang9634.common.di.ServiceInjector;
import com.bang9634.common.util.JsonUtil;
import com.bang9634.websocket.service.WebSocketConnectionService;
import com.bang9634.websocket.service.WebSocketConnectionService.ConnectionResult;
import com.bang9634.websocket.service.WebSocketConnectionService.MessageResult;

/**
 * WebSocket handler for managing chat connections and messages.
 * Handles user connections, disconnections, and message processing.
 */
@WebSocket
public class ChatWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final WebSocketConnectionService webSocketConnectionService;

    /**
     * Constructor.
     * Initializes services via dependency injection.
     */
    public ChatWebSocketHandler() {
        this.webSocketConnectionService = ServiceInjector.getInstance(WebSocketConnectionService.class);
    }

    /**
     * Handle new WebSocket connection.
     * @param session The WebSocket session
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        ConnectionResult result = webSocketConnectionService.handleConnection(session);

        if (!result.isSuccess()) {
            closeSession(session, result.getStatusCode(), result.getMessage());
        }

        sendUserList(session);
        broadcastUserList();
    }

    /**
     * Handle incoming WebSocket message.
     * @param session The WebSocket session
     * @param message The received message
     */
    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        MessageResult result = webSocketConnectionService.handleMessage(session, message);

        if (!result.isSuccess()) {
            logger.debug("Message processing failed: {}", result.getErrorMessage());
            return;
        }

        if (result.isUserListRequest()) {
            sendUserList(session);
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
        logger.info("WebSocket Closed: {} (code: {}, reason: {}",
            session.getRemoteAddress(), statusCode, reason);
        
        webSocketConnectionService.handleDisconnection(session);
        broadcastUserList();        
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
        webSocketConnectionService.handleDisconnection(session);
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

    /**
     * Send the current user list to a specific session.
     * 
     * @param session The WebSocket session to send the user list to
     */
    private void sendUserList(Session session) {
        try {
            if (session != null && session.isOpen()) {
                UserListResponse userList = webSocketConnectionService.getUserListResponse();
                String jsonResponse = JsonUtil.toJson(userList);
                session.getRemote().sendString(jsonResponse);
                logger.debug("Sent user list to session {}: {}",
                    session.getRemoteAddress(), userList.getTotalCount());
            }
        } catch (IOException e) {
            logger.error("Failed to send user list", e);
        }
    }

    /**
     * Broadcast the current user list to all connected users.
     */
    private void broadcastUserList() {
        UserListResponse userList = webSocketConnectionService.getUserListResponse();
        String jsonResponse = JsonUtil.toJson(userList);

        webSocketConnectionService.broadcastUserList(jsonResponse);

        logger.debug("Broadcasted user list: {} users", userList.getTotalCount());
    }
}
