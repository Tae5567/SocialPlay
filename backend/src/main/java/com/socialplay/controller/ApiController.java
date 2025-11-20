package com.socialplay.controller;

import com.socialplay.model.GameSession;
import com.socialplay.model.User;
import com.socialplay.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    @Autowired
    private MatchmakingService matchmakingService;

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        user.setId("user_" + UUID.randomUUID().toString());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/matchmaking/join")
    public ResponseEntity<Map<String, Object>> joinMatchmaking(@RequestBody User user) {
        try {
            matchmakingService.joinQueue(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Joined matchmaking queue");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/matchmaking/leave")
    public ResponseEntity<Map<String, Object>> leaveMatchmaking(@RequestParam String userId) {
        try {
            matchmakingService.leaveQueue(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Left matchmaking queue");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        GameSession session = matchmakingService.getSession(sessionId);
        Map<String, Object> response = new HashMap<>();
        if (session != null) {
            response.put("success", true);
            response.put("session", session);
        } else {
            response.put("success", false);
            response.put("error", "Session not found");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<Map<String, Object>> startSession(@PathVariable String sessionId) {
        matchmakingService.updateSessionStatus(sessionId, "ACTIVE");
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Session started");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> endSession(@PathVariable String sessionId) {
        matchmakingService.endSession(sessionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Session ended");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = matchmakingService.getQueueStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/games")
    public ResponseEntity<List<Map<String, Object>>> getGames() {
        List<Map<String, Object>> games = Arrays.asList(
            createGame("1", "Space Shooter", "Action", "Fast-paced space combat"),
            createGame("2", "Puzzle Master", "Puzzle", "Match-3 puzzle adventure"),
            createGame("3", "Racing Thunder", "Racing", "High-speed racing action"),
            createGame("4", "Strategy Wars", "Strategy", "Build and conquer"),
            createGame("5", "Card Legends", "Card", "Collectible card battles")
        );
        return ResponseEntity.ok(games);
    }

    private Map<String, Object> createGame(String id, String name, String category, String description) {
        Map<String, Object> game = new HashMap<>();
        game.put("id", id);
        game.put("name", name);
        game.put("category", category);
        game.put("description", description);
        game.put("rating", 4.5);
        game.put("downloads", (int)(Math.random() * 1000000));
        return game;
    }
}