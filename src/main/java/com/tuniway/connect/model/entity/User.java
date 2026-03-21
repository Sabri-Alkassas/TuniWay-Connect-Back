package com.tuniway.connect.model.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String email;

    private String password_hash;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
    public User() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    public User(UUID id, String email, String password_hash, Role role, AccountStatus status,Instant lastLoginAt){
        Instant now = Instant.now();
        this.id = id;
        this.email = email;
        this.password_hash = password_hash;
        this.role = role;
        this.status = status;
        this.createdAt = now;
        this.updatedAt = now;
        this.lastLoginAt = lastLoginAt;       
    }
    public UUID getId(){
        return id;
    }
    public void setId(UUID id){
        this.id = id;
        update();
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
        update();
    }
    public String getpassword_hash() {
        return password_hash;
    }
    public void setpassword_hash(String password_hash) {
        this.password_hash = password_hash;
        update();
    }
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
        update();
    }
    public AccountStatus getStatus() {
        return status;
    }
    public void setStatus(AccountStatus status) {
        this.status = status;
        update();
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
    private void update() {
        this.updatedAt = Instant.now();
    }
}
