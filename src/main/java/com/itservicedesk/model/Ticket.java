package com.itservicedesk.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Ticket {
    private String ticketId;
    private String title;
    private String description;
    private String status;
    private String reporterId;
    private String assigneeId;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public Ticket() {}

    // Constructor untuk tiket baru (auto-generate UUID)
    public Ticket(String title, String description, String reporterId, String priority) {
        this.ticketId = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.status = "Open";
        this.reporterId = reporterId;
        this.assigneeId = null;          // belum diklaim
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.resolvedAt = null;
    }

    // Constructor untuk load dari database
    public Ticket(String ticketId, String title, String description, String status,
                  String reporterId, String assigneeId, String priority,
                  LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime resolvedAt) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.reporterId = reporterId;
        this.assigneeId = assigneeId;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
    }

    // Method untuk claim tiket
    public void claim(String assigneeId) {
        this.assigneeId = assigneeId;
        this.status = "In Progress";
        this.updatedAt = LocalDateTime.now();
    }

    // Method resolve tiket
    public void resolve() {
        this.status = "Resolved";
        this.resolvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Method close tiket
    public void close() {
        this.status = "Closed";
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    @Override
    public String toString() {
        return "Ticket{" +
                "tickerId='" + ticketId + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assigneeId='" + assigneeId + '\'' +
                '}';
    }
}