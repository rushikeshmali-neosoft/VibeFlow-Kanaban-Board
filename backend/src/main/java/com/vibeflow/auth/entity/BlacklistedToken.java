package com.vibeflow.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Stores invalidated JWT tokens until their natural expiration time.
 * Any token present in this table must be rejected by JwtAuthenticationFilter,
 * even if the cryptographic signature is valid.
 */
@Entity
@Table(
    name = "token_blacklist",
    indexes = {
        @Index(name = "idx_token_blacklist_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_token_blacklist_expires_at", columnList = "expires_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hex digest of the raw JWT string.
     * Storing the hash (not the token) limits exposure if the table is dumped.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** Email of the user who owned this token (for audit purposes). */
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    /** Copied from the JWT exp claim – used to clean up expired entries. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private LocalDateTime blacklistedAt;
}
