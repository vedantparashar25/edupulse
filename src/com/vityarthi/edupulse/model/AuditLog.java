package com.vityarthi.edupulse.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * System audit trail record capturing critical platform operations.
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private LocalDateTime timestamp;
    private String userId;
    private String action;
    private String status;
    private String details;

    public AuditLog(String id, String userId, String action, String status, String details) {
        this.id = id;
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.action = action;
        this.status = status;
        this.details = details;
    }

    public AuditLog(String id, LocalDateTime timestamp, String userId, String action, String status, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.action = action;
        this.status = status;
        this.details = details;
    }

    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getStatus() { return status; }
    public String getDetails() { return details; }

    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | User: %s | %s (%s)",
                getFormattedTime(), action, userId, details, status);
    }
}
