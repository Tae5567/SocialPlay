package com.socialplay.service;

import com.google.gson.Gson;
import com.socialplay.model.GameSession;
import com.socialplay.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MatchmakingService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private final Gson gson = new Gson();
    private static final String QUEUE_PREFIX = "matchmaking:queue:";
    private static final String SESSION_PREFIX = "session:";
    private static final String USER_PREFIX = "user:";

    public void joinQueue(User user) {
        String queueKey = QUEUE_PREFIX + getQueueCategory(user.getPreferences());
        redisTemplate.opsForValue().set(USER_PREFIX + user.getId(), gson.toJson(user), 30, TimeUnit.MINUTES);
        redisTemplate.opsForZSet().add(queueKey, user.getId(), System.currentTimeMillis());
        tryMatchmaking(queueKey);
    }

    private void tryMatchmaking(String queueKey) {
        Set<Object> waitingUsers = redisTemplate.opsForZSet().range(queueKey, 0, 1);
        if (waitingUsers != null && waitingUsers.size() >= 2) {
            List<String> userIds = waitingUsers.stream().map(Object::toString).collect(Collectors.toList());
            userIds.forEach(userId -> redisTemplate.opsForZSet().remove(queueKey, userId));
            createSession(userIds);
        }
    }

    private void createSession(List<String> userIds) {
        String sessionId = "session_" + UUID.randomUUID().toString();
        GameSession session = new GameSession();
        session.setId(sessionId);
        session.setParticipants(userIds);
        session.setStreamerId(userIds.get(0));
        session.setStatus("WAITING");
        session.setStartTime(System.currentTimeMillis());
        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, gson.toJson(session), 10, TimeUnit.MINUTES);
        userIds.forEach(userId -> {
            String userData = (String) redisTemplate.opsForValue().get(USER_PREFIX + userId);
            if (userData != null) {
                User user = gson.fromJson(userData, User.class);
                user.setCurrentSessionId(sessionId);
                redisTemplate.opsForValue().set(USER_PREFIX + userId, gson.toJson(user), 30, TimeUnit.MINUTES);
            }
        });
    }

    private String getQueueCategory(List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) return "general";
        return preferences.stream().sorted().limit(3).collect(Collectors.joining("_"));
    }

    public GameSession getSession(String sessionId) {
        String data = (String) redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        return data != null ? gson.fromJson(data, GameSession.class) : null;
    }

    public void updateSessionStatus(String sessionId, String status) {
        GameSession session = getSession(sessionId);
        if (session != null) {
            session.setStatus(status);
            redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, gson.toJson(session), 10, TimeUnit.MINUTES);
        }
    }

    public void endSession(String sessionId) {
        GameSession session = getSession(sessionId);
        if (session != null) {
            session.setStatus("COMPLETED");
            session.getParticipants().forEach(userId -> {
                String userData = (String) redisTemplate.opsForValue().get(USER_PREFIX + userId);
                if (userData != null) {
                    User user = gson.fromJson(userData, User.class);
                    user.setCurrentSessionId(null);
                    redisTemplate.opsForValue().set(USER_PREFIX + userId, gson.toJson(user), 30, TimeUnit.MINUTES);
                }
            });
            redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, gson.toJson(session), 1, TimeUnit.HOURS);
        }
    }

    public Map<String, Object> getQueueStats() {
        Map<String, Object> stats = new HashMap<>();
        Set<String> keys = redisTemplate.keys(QUEUE_PREFIX + "*");
        int totalWaiting = 0;
        if (keys != null) {
            for (String key : keys) {
                Long size = redisTemplate.opsForZSet().size(key);
                totalWaiting += (size != null ? size : 0);
            }
        }
        stats.put("totalWaiting", totalWaiting);
        stats.put("queueCount", keys != null ? keys.size() : 0);
        return stats;
    }

    public void leaveQueue(String userId) {
        Set<String> keys = redisTemplate.keys(QUEUE_PREFIX + "*");
        if (keys != null) {
            keys.forEach(key -> redisTemplate.opsForZSet().remove(key, userId));
        }
    }
}