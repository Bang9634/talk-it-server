package com.bang9634.chat.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageRequest {
    private String type;
    private String content;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("username")
    private String username;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isUserListRequest() {
        return "REQUEST_USERS".equals(type);
    }
    
}
