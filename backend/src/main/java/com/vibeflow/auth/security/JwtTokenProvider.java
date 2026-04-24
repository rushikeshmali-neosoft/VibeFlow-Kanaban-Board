package com.vibeflow.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles JWT creation, parsing, and validation.
 *
 * <p><b>Secret configuration:</b> The {@code jwt.secret} property must be at least
 * 32 characters (256 bits) to satisfy the HMAC-SHA256 minimum key length requirement.
 * A startup check enforces this so that a weak default is never silently accepted
 * in production.</p>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /** Minimum key length in bytes required for HMAC-SHA256. */
    private static final int MIN_SECRET_BYTES = 32;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private SecretKey signingKey;

    /**
     * Eagerly validates the secret and initialises the signing key.
     * Fails fast at startup if the secret is too short.
     */
    @PostConstruct
    public void init() {
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                String.format(
                    "JWT secret is too short (%d bytes). Minimum required: %d bytes (256 bits). " +
                    "Set a strong value via the JWT_SECRET environment variable.",
                    secretBytes.length, MIN_SECRET_BYTES
                )
            );
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        log.info("JWT signing key initialised ({} bytes)", secretBytes.length);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}