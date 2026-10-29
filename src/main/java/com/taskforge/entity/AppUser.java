package com.taskforge.entity;

import java.time.LocalDateTime;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AppUser() {
    }

    public AppUser(
            String displayName,
            String email,
            String passwordHash) {

        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    protected void onCreate() {
        normalizeEmail();

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        normalizeEmail();
    }

    private void normalizeEmail() {
        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }
    }

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}