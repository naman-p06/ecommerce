package com.ecommerce.service;

import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiry-ms:604800000}")  // default 7 days
    private long refreshTokenExpiryMs;

    // ── Create ────────────────────────────────────────────────────────
    @Transactional
    public String createRefreshToken(User user) {
        // Delete any existing refresh token for this user (one token per user at a time)
        // Prevents token accumulation if user logs in from multiple devices rapidly
        refreshTokenRepository.deleteByUser(user);

        // Generate a cryptographically random token
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash(rawToken));   // store hash, return raw
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiryMs));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for user {}", user.getEmail());

        // Return raw token to the client — it's never stored raw in the DB
        return rawToken;
    }

    // ── Validate ─────────────────────────────────────────────────────
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            // Clean up the expired token from DB
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        return refreshToken;
    }

    // ── Revoke (logout) ───────────────────────────────────────────────
    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.info("All refresh tokens revoked for user {}", user.getEmail());
    }

    // ── Private: SHA-256 hash ─────────────────────────────────────────
    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK spec — this can never happen
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}