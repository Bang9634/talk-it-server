package com.bang9634.user.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bang9634.common.util.IdGenerator;
import com.bang9634.common.util.NameGenerator;
import com.bang9634.user.model.User;
import com.google.inject.Singleton;

/**
 * Service class for managing user sessions.
 * Handles adding, removing, and retrieving user sessions.
 */
@Singleton
public class UserSessionManager {
    private static final Logger logger = LoggerFactory.getLogger(UserSessionManager.class);

    private final Map<String, User> users; // Map of userId to User
    private final Map<Session, String> sessionToUserId; // Map of Session to userId

    /**
     * Constructor.
     * Initializes the user session manager.
     */
    public UserSessionManager() {
        this.users = new ConcurrentHashMap<>();
        this.sessionToUserId = new ConcurrentHashMap<>();
        logger.info("UserSessionManager initialized.");
    }

    /**
     * Add a new user session.
     * 
     * @param session The WebSocket session
     * @return The created User object
     */
    public User addSession(Session session) {
        String userId = IdGenerator.generateUserId();
        String username = NameGenerator.generateAnonymousName();
        User user = new User(userId, username, session);

        users.put(userId, user);
        sessionToUserId.put(session, userId);

        logger.info("Added new user session: {}({}), connected users {}", 
            username, userId, users.size());
        return user;
    }

    /**
     * Remove a user session.
     * 
     * @param session The WebSocket session to remove
     */
    public void removeSession(Session session) {
        String userId = sessionToUserId.remove(session);
        if (userId != null) {
            User user = users.remove(userId);
            if (user != null) {
                logger.info("Removed user session: {}({}), connected users {}", 
                    user.getUsername(), userId, users.size());
            }
        }
    }

    /**
     * Get a user by their WebSocket session.
     * 
     * @param session The WebSocket session
     * @return The User object associated with the session, or null if not found
     */
    public User getUserBySession(Session session) {
        String userId = sessionToUserId.get(session);
        return userId != null ? users.get(userId) : null;
    }

    /**
     * Get a user by their user ID.
     * 
     * @param userId The user ID
     * @return The User object associated with the user ID, or null if not found
     */
    public User getUserById(String userId) {
        return users.get(userId);
    }

    /**
     * Get all connected users.
     * 
     * @return Collection of all User objects
     */
    public Collection<User> getAllUsers() {
        return users.values();
    }

    /**
     * Get the count of connected users.
     * 
     * @return The number of connected users
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Check if a session is active.
     * 
     * @param session The WebSocket session
     * @return True if the session is active, false otherwise
     */
    public boolean isSessionActive(Session session) {
        return sessionToUserId.containsKey(session);
    }

    /**
     * Get a list of active usernames.
     * 
     * @return List of usernames of all connected users
     */
    public List<String> getActiveUsernames() {
        List<String> usernames = new ArrayList<>();
        users.values().forEach(user -> usernames.add(user.getUsername()));
        return usernames;
    }
}
