package com.vibeflow.auth.service;

import com.vibeflow.auth.entity.BlacklistedToken;
import com.vibeflow.auth.repository.TokenBlacklistRepository;
import com.vibeflow.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;

/**
 * Manages the JWT token blacklist.
 * On logout, the token's SHA-256 hash is stored in the DB alongside its
 * expiry time so the filter can reject it on every subsequent request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Adds the raw JWT to the blacklist.
     *
     * @param rawToken  the full Bearer token string
     * @param userEmail email of the token owner (for audit)
     */
    @Transactional
    public void blacklist(String rawToken, String userEmail) {
        String hash = sha256Hex(rawToken);

        if (tokenBlacklistRepository.existsByTokenHash(hash)) {
            log.debug("Token already blacklisted for user {}", userEmail);
            return;
        }

        Date expiry = jwtTokenProvider.extractExpiration(rawToken);
        LocalDateTime expiresAt = expiry.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        BlacklistedToken entry = BlacklistedToken.builder()
                .tokenHash(hash)
                .userEmail(userEmail)
                .expiresAt(expiresAt)
                .build();

        tokenBlacklistRepository.save(entry);
        log.info("Token blacklisted for user {} (expires {})", userEmail, expiresAt);
    }

    /**
     * Returns {@code true} if the raw JWT has been blacklisted.
     */
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String rawToken) {
        return tokenBlacklistRepository.existsByTokenHash(sha256Hex(rawToken));
    }

    /**
     * Scheduled cleanup – removes rows whose JWTs have naturally expired.
     * Runs every hour so the table stays small.
     */
    @Scheduled(fixedRateString = "PT1H")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = tokenBlacklistRepository.deleteAllExpiredBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Purged {} expired blacklisted tokens", deleted);
        }
    }

    // ── Internal helpers ──────────────────────────────────────

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
