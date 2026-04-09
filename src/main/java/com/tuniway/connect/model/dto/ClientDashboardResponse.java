package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.AccountStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ClientDashboardResponse {
    private boolean success;
    private String message;
    private UUID clientId;
    private String email;
    private String username;
    private String displayName;
    private AccountStatus status;
    private Instant createdAt;
    private Instant lastLoginAt;
    private boolean emailVerified;
    private boolean profileComplete;
    private List<String> missingProfileFields;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    public List<String> getMissingProfileFields() {
        return missingProfileFields;
    }

    public void setMissingProfileFields(List<String> missingProfileFields) {
        this.missingProfileFields = missingProfileFields;
    }
}
