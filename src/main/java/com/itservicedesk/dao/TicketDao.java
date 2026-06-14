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
        String sql = "INSERT INTO tickets * VALUES (?,?,?,?,?,?,?,?,?,?)";
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
            e.printStackTrace();
        }
        return null;
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT  * FROM tickets ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getTicketsByReporter(String reporterId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE reporter_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reporterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getTicketsByAssignee(String assigneeId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE assignee_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assigneeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getUnassignedTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE assignee_id IS NULL ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) tickets.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    // ==================== UPDATE ====================
    public boolean updateTicket(Ticket ticket) {
        String sql = "UPDATE tickets SET title=?, description=?, status=?, reporter_id=?, " +
                "assignee_id=?, priority=?, updated_at=?, resolved_at=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus());
            ps.setString(4, ticket.getReporterId());
            ps.setString(5, ticket.getAssigneeId());
            ps.setString(6, ticket.getPriority());
            ps.setObject(7, LocalDateTime.now());
            ps.setObject(8, ticket.getResolvedAt());
            ps.setString(9, ticket.getTicketId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Claim tiket (hanya jika belum diassign)
    public boolean claimTicket(String ticketId, String assigneeId) {
        String sql = "UPDATE tickets SET assignee_id=?, status='In Progress', updated_at=? WHERE id=? AND assignee_id IS NULL";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assigneeId);
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resolveTicket(String ticketId) {
        String sql = "UPDATE tickets SET status='Resolved', resolved_at=?, updated_at=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setObject(2, LocalDateTime.now());
            ps.setString(3, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeTicket(String ticketId) {
        String sql = "UPDATE tickets SET status='Closed', updated_at=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setString(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== DELETE ====================
    public boolean deleteTicket(String ticketId) {
        String sql = "DELETE FROM tickets WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== HELPER ====================
    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getString("ticket_id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setStatus(rs.getString("status"));
        ticket.setReporterId(rs.getString("reporter_id"));
        ticket.setAssigneeId(rs.getString("assignee_id"));
        ticket.setPriority(rs.getString("priority"));

        Timestamp ts;
        ts = rs.getTimestamp("created_at");
        if (ts != null) ticket.setCreatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("updated_at");
        if (ts != null) ticket.setUpdatedAt(ts.toLocalDateTime());
        ts = rs.getTimestamp("resolved_at");
        if (ts != null) ticket.setResolvedAt(ts.toLocalDateTime());

        return ticket;
    }
}