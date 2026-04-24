package com.vibeflow.auth.service;

import com.vibeflow.auth.entity.BlacklistedToken;
import com.vibeflow.auth.repository.TokenBlacklistRepository;
import com.vibeflow.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService – TDD")
class TokenBlacklistServiceTest {

    @Mock TokenBlacklistRepository tokenBlacklistRepository;
    @Mock JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    TokenBlacklistService tokenBlacklistService;

    private static final String RAW_TOKEN = "header.payload.signature";
    private static final String USER_EMAIL = "user@vibeflow.com";

    // SHA-256 of "header.payload.signature"
    private static final String EXPECTED_HASH =
        "6c4f24f5b671f95640d5fcaf1b31e18b08d5e2b7c7c52e2de3f2e1e4c9eaf5a3";

    @BeforeEach
    void setUp() {
        Date futureExpiry = new Date(System.currentTimeMillis() + 3_600_000L); // +1h
        when(jwtTokenProvider.extractExpiration(RAW_TOKEN)).thenReturn(futureExpiry);
    }

    // ── blacklist() ───────────────────────────────────────────

    @Nested
    @DisplayName("blacklist()")
    class BlacklistTests {

        @Test
        @DisplayName("First blacklist saves entry with SHA-256 hash")
        void blacklist_firstTime_savesHashedEntry() {
            when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

            tokenBlacklistService.blacklist(RAW_TOKEN, USER_EMAIL);

            ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
            verify(tokenBlacklistRepository).save(captor.capture());

            BlacklistedToken saved = captor.getValue();
            assertThat(saved.getUserEmail()).isEqualTo(USER_EMAIL);
            assertThat(saved.getTokenHash()).isNotBlank();
            assertThat(saved.getTokenHash()).hasSize(64); // SHA-256 hex = 64 chars
            assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("Duplicate blacklist call is idempotent – no second save")
        void blacklist_duplicate_isIdempotent() {
            when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);

            tokenBlacklistService.blacklist(RAW_TOKEN, USER_EMAIL);

            verify(tokenBlacklistRepository, never()).save(any());
        }

        @Test
        @DisplayName("Raw token is never stored – only its hash")
        void blacklist_storesHashNotRawToken() {
            when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

            tokenBlacklistService.blacklist(RAW_TOKEN, USER_EMAIL);

            ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
            verify(tokenBlacklistRepository).save(captor.capture());

            // The stored hash must not equal the raw token
            assertThat(captor.getValue().getTokenHash()).isNotEqualTo(RAW_TOKEN);
        }

        @Test
        @DisplayName("expiresAt is copied from JWT expiration claim")
        void blacklist_expiresAtMatchesJwtExpiry() {
            Date jwtExpiry = new Date(System.currentTimeMillis() + 7_200_000L); // +2h
            when(jwtTokenProvider.extractExpiration(RAW_TOKEN)).thenReturn(jwtExpiry);
            when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);

            tokenBlacklistService.blacklist(RAW_TOKEN, USER_EMAIL);

            ArgumentCaptor<BlacklistedToken> captor = ArgumentCaptor.forClass(BlacklistedToken.class);
            verify(tokenBlacklistRepository).save(captor.capture());

            LocalDateTime expected = jwtExpiry.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            assertThat(captor.getValue().getExpiresAt()).isEqualToIgnoringNanos(expected);
        }
    }

    // ── isBlacklisted() ───────────────────────────────────────

    @Nested
    @DisplayName("isBlacklisted()")
    class IsBlacklistedTests {

        @Test
        @DisplayName("Returns true when hash is in DB")
        void isBlacklisted_hashExists_returnsTrue() {
            when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(true);
            assertThat(tokenBlacklistService.isBlacklisted(RAW_TOKEN)).isTrue();
        }

        @Test
        @DisplayName("Returns false when hash is not in DB")
        void isBlacklisted_hashAbsent_returnsFalse() {
            when(tokenBlacklistRepository.existsByTokenHash(anyString())).thenReturn(false);
            assertThat(tokenBlacklistService.isBlacklisted(RAW_TOKEN)).isFalse();
        }
    }

    // ── purgeExpiredTokens() ──────────────────────────────────

    @Nested
    @DisplayName("purgeExpiredTokens()")
    class PurgeTests {

        @Test
        @DisplayName("Calls deleteAllExpiredBefore with current time")
        void purgeExpiredTokens_callsRepository() {
            when(tokenBlacklistRepository.deleteAllExpiredBefore(any())).thenReturn(3);

            tokenBlacklistService.purgeExpiredTokens();

            verify(tokenBlacklistRepository).deleteAllExpiredBefore(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Does not throw even when repository returns 0")
        void purgeExpiredTokens_zeroDeleted_noException() {
            when(tokenBlacklistRepository.deleteAllExpiredBefore(any())).thenReturn(0);
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> tokenBlacklistService.purgeExpiredTokens());
        }
    }
}
