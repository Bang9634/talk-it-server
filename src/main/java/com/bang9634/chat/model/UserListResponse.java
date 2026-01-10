package com.bang9634.chat.model;

import java.util.List;

public class UserListResponse {
    private String type = "USER_LIST";
    private List<UserInfo> users;
    private int totalCount;
    private long timestamp;

    public static class UserInfo {
        private String userId;
        private String username;
        private String maskedIp;
        private long connectedAt;
        private boolean isAuthenticated;

        public UserInfo() {}
        
        public UserInfo(String userId, String username, String maskedIp,
                        long connectedAt, boolean isAuthenticated) {
            this.userId = userId;
            this.username = username;
            this.maskedIp = maskedIp;
            this.connectedAt = connectedAt;
            this.isAuthenticated = isAuthenticated;
        }

        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getMaskedIp() { return maskedIp; }
        public void setMaskedIp(String maskedIp) { this.maskedIp = maskedIp; }

        public long getConnectedAt() { return connectedAt; }
        public void setConnectedAt(long connectedAt) { this.connectedAt = connectedAt; }

        public boolean isAuthenticated() { return isAuthenticated; }
        public void setAuthenticated(boolean authenticated) { isAuthenticated = authenticated; }
    }

    public UserListResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public UserListResponse(List<UserInfo> users, int totalCount) {
        this.users = users;
        this.totalCount = totalCount;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<UserInfo> getUsers() { return users; }
    public void setUsers(List<UserInfo> users) { this.users = users; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
