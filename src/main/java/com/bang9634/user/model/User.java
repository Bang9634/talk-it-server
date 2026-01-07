package com.bang9634.user.model;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Model class representing a user.
 * Includes user ID, username, session information, and connection timestamp.
 */
public class User {
    private String userId;
    private String username;
    private Session session;
    private long connectedAt;
    private String ipAddress;
    private String maskedIp;

    /**
     * Parameterized constructor.
     * @param userId The Unique ID of the user
     * @param username The username of the user
     * @param session The session associated with the user
     * @param connectedAt The timestamp when the user connected
     * @param ipAddress The IP address of the user
     * @param maskedIp The masked IP address for privacy
     */
    public User(String userId, String username, Session session, String ipAddress, String maskedIp) {
        this.userId = userId;
        this.username = username;
        this.session = session;
        this.connectedAt = System.currentTimeMillis();
        this.ipAddress = ipAddress;
        this.maskedIp = maskedIp;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public long getConnectedAt() { return connectedAt; }
    public void setConnectedAt(long connectedAt) { this.connectedAt = connectedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMaskedIp() { return maskedIp; }
    public void setMaskedIp(String maskedIp) { this.maskedIp = maskedIp; }

    public String getDisplayName() {
        return username + " " + maskedIp;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", maskedIp='" + maskedIp + '\'' +
                ", connectedAt=" + connectedAt +
                '}';
    }
}
