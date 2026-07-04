package com.itservicedesk.model;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private String employeeId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String title;
    private String assignedDevice;
    private String passwordHash;
    private String role;
    private String department;
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // Constructor blank
    public User() {
    }

    // Constructor untuk objek baru (registrasi)
    public User(String fullName, String username, String email, String phone, String title, String assignedDevice, String plainPassword, String role, String department) {
        this.employeeId = "EMP-" + UUID.randomUUID();
        this.fullName = fullName;
        this.username = username;
        this.email = email;  // ← perbaikan: email sekarang tersimpan
        this.phone = phone;
        this.title = title;
        this.assignedDevice= assignedDevice;
        this.passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        this.role = role;
        this.department = department;
        this.isActive = true;
        this.isLocked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = null;
    }

    // Constructor untuk load dari database (semua field) dipakai sama dao
    public User(String employeeId, String fullName, String username, String email,String phone, String title, String assignedDevice, String passwordHash,
                String role, String department, Boolean isActive, Boolean isLocked,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.title = title;
        this.assignedDevice= assignedDevice;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.isActive = isActive;
        this.isLocked = isLocked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Update password (digunakan untuk ganti password biasa)
    public void updatePassword(String newPlainPassword) {
        this.passwordHash = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
        this.updatedAt = LocalDateTime.now();
    }

    // Getter dan Setter (semua field)
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAssignedDevice() { return assignedDevice; }
    public void setAssignedDevice(String assignedDevice) { this.assignedDevice = assignedDevice; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @Override
    public String toString() {
        return "User{" +
                "employeeId='" + employeeId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", department='" + department + '\'' +
                ", isActive=" + isActive +
                ", isLocked=" + isLocked +
                '}';
    }
}