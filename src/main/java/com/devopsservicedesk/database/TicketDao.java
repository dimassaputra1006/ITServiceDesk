package com.devopsservicedesk.database;

import com.devopsservicedesk.models.Ticket;

import java.sql.*;

public class TicketDao{
    public void insertTicket(Ticket ticketBaru){
        String sql = "INSERT INTO tickets (judulTicket, descProblem, statusTicket, idReporter,idTechnician) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, ticketBaru.getJudulTicket());
            pstmt.setString(2, ticketBaru.getDescProbelm());
            pstmt.setString(3, ticketBaru.getStatusTicket());
            pstmt.setInt(4, ticketBaru.getIdReporter());
            pstmt.setInt(5, ticketBaru.getIdTechnician());

            pstmt.executeUpdate();
            System.out.println("Ticket pelaporan: " + ticketBaru.getJudulTicket() + " berhasil ditambahkan");
        }catch (SQLException e){
            System.out.println("gagal menambahkan data: " + e.getMessage());
        }
    }
}