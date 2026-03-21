package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.Role;

import java.time.Instant;
import java.util.UUID;

public class LoginResponse {
    private UUID id;
    private String email;
    private Role role;
    private AccountStatus status;
    private Instant lastLoginAt;
    private boolean authenticated;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(UUID id, String email, Role role, AccountStatus status, Instant lastLoginAt, boolean authenticated, String message) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.authenticated = authenticated;
        this.message = message;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
