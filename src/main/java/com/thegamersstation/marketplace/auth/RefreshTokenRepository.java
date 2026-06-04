package com.thegamersstation.marketplace.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its jti hash.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Revoke all tokens in a family (theft detection).
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.tokenFamily = :family")
    int revokeFamily(@Param("family") String tokenFamily);

    /**
     * Revoke all refresh tokens for a user (e.g., on password change or account ban).
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.userId = :userId")
    int revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Delete expired tokens (cleanup).
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") Instant now);
}
