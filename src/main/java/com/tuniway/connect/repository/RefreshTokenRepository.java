package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
        Optional<RefreshToken> findByTokenHash(String tokenHash);

        @Modifying
        @Query("""
                        UPDATE RefreshToken rt
                        SET rt.revoked = true
                        WHERE rt.tokenHash = :tokenHash
                            AND rt.revoked = false
                            AND rt.expiresAt > :now
                        """)
        int revokeIfActive(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

        @Modifying
        @Query("""
                        UPDATE RefreshToken rt
                        SET rt.revoked = true
                        WHERE rt.tokenHash = :tokenHash
                            AND rt.revoked = false
                        """)
        int revokeIfNotRevoked(@Param("tokenHash") String tokenHash);

        @Modifying
        int deleteByRevokedTrueOrExpiresAtBefore(Instant now);

        @Modifying
        int deleteByUserId(UUID userId);
}
