package com.vibeflow.auth.repository;

import com.vibeflow.auth.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<BlacklistedToken, Long> {

    /** Returns true if the given token hash is in the blacklist. */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Purges rows whose JWT has already expired naturally.
     * Called on a scheduled basis so the table doesn't grow indefinitely.
     */
    @Modifying
    @Query("DELETE FROM BlacklistedToken t WHERE t.expiresAt < :now")
    int deleteAllExpiredBefore(LocalDateTime now);
}
