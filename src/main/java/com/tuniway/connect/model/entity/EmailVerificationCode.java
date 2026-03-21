package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCode {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    private Instant expiresAt;

    @Column(nullable = false)
    private Boolean consumed;

    @Column(nullable = false)
    private Instant createdAt;

    public EmailVerificationCode() {
        this.consumed = false;
        this.createdAt = Instant.now();
    }

    public EmailVerificationCode(UUID id, String email, String code, Instant expiresAt, Boolean consumed, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
        this.consumed = consumed;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getConsumed() {
        return consumed;
    }

    public void setConsumed(Boolean consumed) {
        this.consumed = consumed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
