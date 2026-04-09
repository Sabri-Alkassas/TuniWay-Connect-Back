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

    @Column(name = "admin_code", unique = true)
    private String adminCode;

    @Column(name = "full_name")
    private String fullName;

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

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
