package com.socialplay;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userToSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonObject json = gson.fromJson(payload, JsonObject.class);
        String type = json.get("type").getAsString();

        switch (type) {
            case "join" -> handleJoin(session, json);
            case "offer" -> handleOffer(session, json);
            case "answer" -> handleAnswer(session, json);
            case "ice-candidate" -> handleIceCandidate(session, json);
            case "leave" -> handleLeave(session, json);
        }
    }

    private void handleJoin(WebSocketSession session, JsonObject json) throws IOException {
        String roomId = json.get("roomId").getAsString();
        String userId = json.get("userId").getAsString();
        sessionRooms.put(session.getId(), roomId);
        userToSession.put(userId, session.getId());

        JsonObject response = new JsonObject();
        response.addProperty("type", "user-joined");
        response.addProperty("userId", userId);
        broadcastToRoom(roomId, response, session.getId());

        JsonObject ack = new JsonObject();
        ack.addProperty("type", "joined");
        ack.addProperty("roomId", roomId);
        session.sendMessage(new TextMessage(gson.toJson(ack)));
    }

    private void handleOffer(WebSocketSession session, JsonObject json) throws IOException {
        forwardMessage(json, "offer");
    }

    private void handleAnswer(WebSocketSession session, JsonObject json) throws IOException {
        forwardMessage(json, "answer");
    }

    private void handleIceCandidate(WebSocketSession session, JsonObject json) throws IOException {
        forwardMessage(json, "ice-candidate");
    }

    private void forwardMessage(JsonObject json, String type) throws IOException {
        String targetUserId = json.get("to").getAsString();
        String targetSessionId = userToSession.get(targetUserId);
        if (targetSessionId != null) {
            WebSocketSession targetSession = sessions.get(targetSessionId);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage(gson.toJson(json)));
            }
        }
    }

    private void handleLeave(WebSocketSession session, JsonObject json) {
        String roomId = sessionRooms.get(session.getId());
        if (roomId != null) {
            JsonObject response = new JsonObject();
            response.addProperty("type", "user-left");
            response.addProperty("userId", json.get("userId").getAsString());
            try {
                broadcastToRoom(roomId, response, session.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cleanup(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cleanup(session);
        System.out.println("WebSocket disconnected: " + session.getId());
    }

    private void cleanup(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionRooms.remove(session.getId());
        userToSession.values().removeIf(v -> v.equals(session.getId()));
    }

    private void broadcastToRoom(String roomId, JsonObject message, String excludeSessionId) throws IOException {
        for (Map.Entry<String, String> entry : sessionRooms.entrySet()) {
            if (entry.getValue().equals(roomId) && !entry.getKey().equals(excludeSessionId)) {
                WebSocketSession targetSession = sessions.get(entry.getKey());
                if (targetSession != null && targetSession.isOpen()) {
                    targetSession.sendMessage(new TextMessage(gson.toJson(message)));
                }
            }
        }
    }
}