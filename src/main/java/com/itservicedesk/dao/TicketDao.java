package com.itservicedesk.dao;

import com.itservicedesk.model.Ticket;
import com.itservicedesk.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDao {

    // ==================== CREATE ====================
    public boolean insertTicket(Ticket ticket) {
        String sql = """
            INSERT INTO tickets (ticket_id, title, description, status, reporter_id,
                                 assignee_id, priority, created_at, updated_at, resolved_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticket.getTicketId());
            ps.setString(2, ticket.getTitle());
            ps.setString(3, ticket.getDescription());
            ps.setString(4, ticket.getStatus());
            ps.setString(5, ticket.getReporterId());
            ps.setString(6, ticket.getAssigneeId());
            ps.setString(7, ticket.getPriority());
            ps.setObject(8, ticket.getCreatedAt());
            ps.setObject(9, ticket.getUpdatedAt());
            ps.setObject(10, ticket.getResolvedAt());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error insert ticket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== READ ====================
    public Ticket findById(String ticketId) {
        String sql = "SELECT * FROM tickets WHERE ticket_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ticketId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error find ticketId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error get all tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getHistoryTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = """
            SELECT * FROM tickets
            WHERE status IN ('Resolved', 'Closed')
            ORDER BY updated_at DESC
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error get history tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getQueueTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = """
            SELECT * FROM tickets WHERE status = 'Open'
            ORDER BY
                CASE priority
                    WHEN 'Critical' THEN 1
                    WHEN 'High' THEN 2
                    WHEN 'Medium' THEN 3
                    WHEN 'Low' THEN 4
                    ELSE 5
                END,
                created_at ASC
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error get queue tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getActiveTickets(String assigneeId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = """
            SELECT * FROM tickets
            WHERE status = 'In Progress' AND assignee_id = ?
            ORDER BY updated_at DESC
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, assigneeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error get active tickets: " + e.getMessage());
            e.printStackTrace();
        }
        return tickets;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE status = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error count by status: " + e.getMessage());
        }
        return 0;
    }

    public int countActiveByAssignee(String assigneeId) {
        if (assigneeId == null) return 0;
        String sql = "SELECT COUNT(*) FROM tickets WHERE status = 'In Progress' AND assignee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, assigneeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error count active tickets: " + e.getMessage());
        }
        return 0;
    }

    // ==================== UPDATE STATUS ====================
    public boolean claimTicket(String ticketId, String assigneeId) {
        String sql = """
            UPDATE tickets 
            SET assignee_id = ?, status = 'In Progress', updated_at = ? 
            WHERE ticket_id = ?
              AND assignee_id IS NULL
              AND status = 'Open'
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, assigneeId);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error claim ticket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean resolveTicket(String ticketId) {
        String sql = """
            UPDATE tickets 
            SET status = 'Resolved', resolved_at = ?, updated_at = ? 
            WHERE ticket_id = ?
              AND status = 'In Progress'
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();
            ps.setObject(1, now);
            ps.setObject(2, now);
            ps.setString(3, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resolve ticket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean releaseTicket(String ticketId, String assigneeId) {
        String sql = """
            UPDATE tickets
            SET assignee_id = NULL, status = 'Open', updated_at = ?
            WHERE ticket_id = ? AND assignee_id = ? AND status = 'In Progress'
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setString(2, ticketId);
            ps.setString(3, assigneeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error release ticket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeTicket(String ticketId) {
        String sql = """
            UPDATE tickets 
            SET status = 'Closed', updated_at = ? 
            WHERE ticket_id = ?
              AND status IN ('In Progress', 'Resolved')
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, LocalDateTime.now());
            ps.setString(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error close ticket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== HELPER ====================
    private Ticket mapRow(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getString("ticket_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getString("reporter_id"),
                rs.getString("assignee_id"),
                rs.getString("priority"),
                getLocalDateTime(rs, "created_at"),
                getLocalDateTime(rs, "updated_at"),
                getLocalDateTime(rs, "resolved_at")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        // Cara 1: Pakai String dulu (paling aman untuk SQLite)
        String timestampStr = rs.getString(column);
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle format dengan nano seconds atau tanpa
            if (timestampStr.contains(".")) {
                // Potong nano seconds kalau terlalu panjang
                if (timestampStr.length() > 23) {
                    timestampStr = timestampStr.substring(0, 23);
                }
                return LocalDateTime.parse(timestampStr);
            } else {
                return LocalDateTime.parse(timestampStr);
            }
        } catch (Exception e) {
            System.err.println("Gagal parse timestamp kolom " + column + ": " + timestampStr);
            // Fallback: coba pakai Timestamp
            Timestamp ts = rs.getTimestamp(column);
            return ts != null ? ts.toLocalDateTime() : null;
        }
    }
}