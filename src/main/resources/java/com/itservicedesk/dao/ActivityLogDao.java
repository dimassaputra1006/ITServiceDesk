package com.itservicedesk.dao;

import com.itservicedesk.config.DatabaseConfig;
import com.itservicedesk.model.ActivityLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public boolean insert(ActivityLog log) {
        String sql = """
            INSERT INTO activity_log (ticket_id, analyst_id, action, message, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, log.getTicketId());
            ps.setString(2, log.getAnalystId());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getMessage());
            ps.setObject(5, log.getCreatedAt());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insert activity log: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getRecent(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_log ORDER BY created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) logs.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error get activity logs: " + e.getMessage());
        }
        return logs;
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getLong("id"),
                rs.getString("ticket_id"),
                rs.getString("analyst_id"),
                rs.getString("action"),
                rs.getString("message"),
                parseDateTime(rs.getString("created_at"))
        );
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            if (value.length() > 23) value = value.substring(0, 23);
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }
}
