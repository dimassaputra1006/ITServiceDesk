package com.itservicedesk.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private long id;
    private String ticketId;
    private String analystId;
    private String action;
    private String message;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public ActivityLog(String ticketId, String analystId, String action, String message) {
        this.ticketId = ticketId;
        this.analystId = analystId;
        this.action = action;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLog(long id, String ticketId, String analystId, String action,
                       String message, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.analystId = analystId;
        this.action = action;
        this.message = message;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getAnalystId() { return analystId; }
    public void setAnalystId(String analystId) { this.analystId = analystId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
