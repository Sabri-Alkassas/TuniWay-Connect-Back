package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "admin_profiles")
public class AdminProfile {
    @Id
    private UUID userId;

    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;

    @Column(name = "two_factor_secret_encrypted")
    private String twoFactorSecretEncrypted;

    public AdminProfile() {
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getTwoFactorSecretEncrypted() {
        return twoFactorSecretEncrypted;
    }

    public void setTwoFactorSecretEncrypted(String twoFactorSecretEncrypted) {
        this.twoFactorSecretEncrypted = twoFactorSecretEncrypted;
    }
}