package com.devicemgmt.server.security;

import com.devicemgmt.server.config.AppConfig;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static final TokenManager INSTANCE = new TokenManager();

    private record TokenInfo(int userId, String username, String role, LocalDateTime expiry) {}

    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    private TokenManager() {}

    public static TokenManager getInstance() { return INSTANCE; }

    public String createToken(int userId, String username, String role) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(AppConfig.getTokenExpiryHours());
        tokens.put(token, new TokenInfo(userId, username, role, expiry));
        return token;
    }

    public boolean isValid(String token) {
        if (token == null) return false;
        TokenInfo info = tokens.get(token);
        if (info == null) return false;
        if (LocalDateTime.now().isAfter(info.expiry())) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public String getUsername(String token) {
        TokenInfo info = tokens.get(token);
        return info != null ? info.username() : null;
    }

    public String getRole(String token) {
        TokenInfo info = tokens.get(token);
        return info != null ? info.role() : null;
    }

    public int getUserId(String token) {
        TokenInfo info = tokens.get(token);
        return info != null ? info.userId() : -1;
    }

    public boolean isAdmin(String token) {
        return "ADMIN".equals(getRole(token));
    }

    public void invalidate(String token) {
        tokens.remove(token);
    }

    public void cleanExpired() {
        LocalDateTime now = LocalDateTime.now();
        tokens.entrySet().removeIf(e -> now.isAfter(e.getValue().expiry()));
    }
}
