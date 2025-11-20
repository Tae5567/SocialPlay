package com.socialplay.model;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private String id;
    private String gameId;
    private String gameName;
    private List<String> participants;
    private String streamerId;
    private long startTime;
    private long duration = 60000;
    private String status;
    private List<String> genres;

    public GameSession() {
        this.participants = new ArrayList<>();
    }

    public GameSession(String id, String gameId, String gameName, String streamerId) {
        this.id = id;
        this.gameId = gameId;
        this.gameName = gameName;
        this.streamerId = streamerId;
        this.participants = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.status = "WAITING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public String getStreamerId() { return streamerId; }
    public void setStreamerId(String streamerId) { this.streamerId = streamerId; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}