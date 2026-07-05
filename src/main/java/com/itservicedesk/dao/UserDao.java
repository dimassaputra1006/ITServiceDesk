package com.itservicedesk.dao;

import com.itservicedesk.model.User;
import com.itservicedesk.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // ==================== CREATE ====================
    public boolean insertUser(User user) {
        String sql = """
        INSERT INTO users (employee_id, full_name, username, email, phone,
                           assigned_device, password_hash, role, department, avatar_path,
                           is_active, is_locked, created_at, updated_at, last_login_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmployeeId());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getAssignedDevice());
            pstmt.setString(7, user.getPasswordHash());
            pstmt.setString(8, user.getRole());
            pstmt.setString(9, user.getDepartment());
            pstmt.setString(10, user.getAvatarPath());
            pstmt.setBoolean(11, user.getIsActive());
            pstmt.setBoolean(12, user.getIsLocked());
            pstmt.setObject(13, user.getCreatedAt());
            pstmt.setObject(14, user.getUpdatedAt());
            pstmt.setObject(15, user.getLastLoginAt());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            return false;
        }
    }

    // ==================== READ ====================
    public User findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM users WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by employeeId: " + e.getMessage());
        }
        return null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }

    public List<User> getActiveItStaff() {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT * FROM users
            WHERE is_active = true
              AND lower(department) = 'it'
            ORDER BY full_name ASC
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active IT staff: " + e.getMessage());
        }
        return users;
    }

    // ==================== UPDATE ====================
    public boolean updateUser(User user) {
        String sql = """
        UPDATE users 
        SET full_name = ?, username = ?, email = ?, phone = ?, 
            assigned_device = ?, password_hash = ?, role = ?, department = ?, 
            avatar_path = ?, is_active = ?, is_locked = ?, updated_at = ?, last_login_at = ? 
        WHERE employee_id = ?
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getAssignedDevice());
            pstmt.setString(6, user.getPasswordHash());
            pstmt.setString(7, user.getRole());
            pstmt.setString(8, user.getDepartment());
            pstmt.setString(9, user.getAvatarPath());
            pstmt.setBoolean(10, user.getIsActive());
            pstmt.setBoolean(11, user.getIsLocked());
            pstmt.setObject(12, LocalDateTime.now());
            pstmt.setObject(13, user.getLastLoginAt());
            pstmt.setString(14, user.getEmployeeId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    // Updates only the profile photo path — handy for a dedicated "change photo" action later
    public boolean updateAvatar(String employeeId, String avatarPath) {
        String sql = "UPDATE users SET avatar_path = ?, updated_at = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, avatarPath);
            pstmt.setObject(2, LocalDateTime.now());
            pstmt.setString(3, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating avatar: " + e.getMessage());
            return false;
        }
    }

    public boolean updateLastLogin(String employeeId, LocalDateTime lastLoginAt) {
        String sql = "UPDATE users SET last_login_at = ?, updated_at = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, lastLoginAt);
            pstmt.setObject(2, LocalDateTime.now());
            pstmt.setString(3, employeeId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            return false;
        }
    }

    public boolean lockUser(String employeeId) {
        String sql = "UPDATE users SET is_locked = true, updated_at = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, LocalDateTime.now());
            pstmt.setString(2, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error locking user: " + e.getMessage());
            return false;
        }
    }

    public boolean unlockUser(String employeeId) {
        String sql = "UPDATE users SET is_locked = false, updated_at = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, LocalDateTime.now());
            pstmt.setString(2, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error unlocking user: " + e.getMessage());
            return false;
        }
    }

    public boolean resetPassword(String employeeId, String newPlainPassword) {
        String hashed = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashed);
            pstmt.setObject(2, LocalDateTime.now());
            pstmt.setString(3, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }

    // ==================== DELETE ====================
    public boolean deleteUser(String employeeId) {
        String sql = "DELETE FROM users WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId);
            int affected = pstmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    // ==================== Helper ====================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setEmployeeId(rs.getString("employee_id"));
        user.setFullName(rs.getString("full_name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setAssignedDevice(rs.getString("assigned_device"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setDepartment(rs.getString("department"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setIsActive(rs.getBoolean("is_active"));
        user.setIsLocked(rs.getBoolean("is_locked"));

        user.setCreatedAt(parseDateTime(rs.getString("created_at")));
        user.setUpdatedAt(parseDateTime(rs.getString("updated_at")));
        user.setLastLoginAt(parseDateTime(rs.getString("last_login_at")));

        return user;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            if (value.length() > 23) value = value.substring(0, 23);
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}