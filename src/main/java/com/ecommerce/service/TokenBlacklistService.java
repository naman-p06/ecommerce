package com.ecommerce.service;

import com.ecommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "blacklist:";

    public void blacklist(String token) {
        try {
            Date expiry = jwtUtil.extractExpiration(token);
            long ttlSeconds = Duration.between(Instant.now(), expiry.toInstant()).getSeconds();

            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofSeconds(ttlSeconds));
                log.info("Token blacklisted in Redis, TTL={}s", ttlSeconds);
            }
            // if ttlSeconds <= 0, token is already expired — no point storing it
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}