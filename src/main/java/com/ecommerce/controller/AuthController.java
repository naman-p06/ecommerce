package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RefreshTokenRequest;
import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.RefreshTokenService;
import com.ecommerce.service.TokenBlacklistService;
import com.ecommerce.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil              jwtUtil;
    private final RefreshTokenService  refreshTokenService;
    private final TokenBlacklistService blacklistService;
    private final UserRepository       userRepository;

    @Value("${jwt.access-token.expiry-ms:3600000}")
    private long accessTokenExpiryMs;

    // ── POST /api/auth/refresh ────────────────────────────────────────
    // Client calls this when their access token expires.
    // They send the refresh token and get a brand new access token back.
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        // 1. Validate refresh token (checks DB, expiry, revoked flag)
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        // 2. Issue new access token
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(), user.getRole().name());

        // 3. Rotate refresh token — old one deleted, new one issued
        // This is called "refresh token rotation" — if an attacker steals
        // a refresh token and tries to use it after the real user already
        // rotated it, their token is invalid and you can detect the theft.
        String newRefreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponse response = new AuthResponse(
                newAccessToken, newRefreshToken, accessTokenExpiryMs / 1000);

        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────
    // Invalidates BOTH the access token (blacklist) and refresh token (DB delete).
    // After this call the user is fully logged out — even if the access token
    // hasn't expired yet, it won't pass the blacklist check in JwtFilter.
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            Principal principal) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Add current access token to blacklist so it can't be used again
            blacklistService.blacklist(token);
        }

        // Revoke refresh token from DB
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        refreshTokenService.revokeByUser(user);

        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}