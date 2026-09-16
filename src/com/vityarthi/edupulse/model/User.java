package com.vityarthi.edupulse.model;

import com.vityarthi.edupulse.common.Role;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract Base Class representing an authenticated entity in EduPulse.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected String salt;
    protected Role role;
    protected LocalDateTime createdAt;
    protected boolean active;

    public User(String id, String name, String email, String passwordHash, String salt, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public User(String id, String name, String email, String passwordHash, String salt, Role role, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.active = active;
    }

    // Abstract method demonstrating Polymorphism in concrete subclasses
    public abstract String getProfileSummary();

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s", role, name, email, id);
    }
}
