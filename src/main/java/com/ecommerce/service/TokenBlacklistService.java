package com.ecommerce.service;

import com.ecommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final JwtUtil jwtUtil;

    // ConcurrentHashMap — thread-safe without locking the whole map.
    // Key = raw JWT string, Value = expiry timestamp (ms since epoch)
    // We store expiry so we can clean up tokens that have already expired naturally.
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token) {
        try {
            Date expiry = jwtUtil.extractExpiration(token);
            blacklist.put(token, expiry.getTime());
            log.info("Token blacklisted, expires at {}", expiry);
        } catch (Exception e) {
            // If token is already invalid/expired, no need to blacklist it
            log.warn("Attempted to blacklist an invalid token: {}", e.getMessage());
        }
    }

    // Called in JwtFilter on every request
    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    // Runs every 30 minutes — removes tokens whose expiry has already passed.
    // Without this, the map grows forever because logout tokens are never removed.
    // Once a token is expired it can't be used anyway, so storing it is pointless.
    @Scheduled(fixedRateString = "${jwt.blacklist.cleanup-ms:1800000}")
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int before = blacklist.size();

        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);

        int removed = before - blacklist.size();
        if (removed > 0) {
            log.info("Blacklist cleanup: removed {} expired tokens", removed);
        }
    }
}