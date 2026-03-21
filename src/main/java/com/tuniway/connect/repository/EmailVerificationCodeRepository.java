package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, UUID> {
    @Query(value = "SELECT * FROM email_verification_codes WHERE email = :email AND consumed = FALSE ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<EmailVerificationCode> findLatestPendingByEmail(@Param("email") String email);
}
