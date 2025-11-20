package com.socialplay.model;

import java.util.List;

public class User {
    private String id;
    private String username;
    private List<String> preferences;
    private double rating;
    private String currentSessionId;

    public User() {}

    public User(String id, String username, List<String> preferences) {
        this.id = id;
        this.username = username;
        this.preferences = preferences;
        this.rating = 0.0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getPreferences() { return preferences; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public String getCurrentSessionId() { return currentSessionId; }
    public void setCurrentSessionId(String sessionId) { this.currentSessionId = sessionId; }
}