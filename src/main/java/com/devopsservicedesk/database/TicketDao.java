package com.devopsservicedesk.database;

import com.devopsservicedesk.models.Ticket;
import com.devopsservicedesk.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            System.out.println("gagal menambahkan ticket: " + e.getMessage());
        }
    }

    public Ticket getTicketById(int idTicket){
        String sql = "select * from tickets where idTicket = ?";
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, idTicket);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return  new Ticket(
                        rs.getInt("idTicket"),
                        rs.getString("JudulTicket"),
                        rs.getString("DescProbelm"),
                        rs.getString("statusTicket"),
                        rs.getInt("idReporter"),
                        rs.getInt("idTechnician")
                );
            }
        }catch (SQLException e){
            System.out.println("gagal mencari user: "+ e.getMessage());
        }
        return null;
    }

    public List<Ticket> getAllTicket(){
        List<Ticket> ticketList = new ArrayList<>();
        String sql = "Select * from tickets";
        try(Connection conn = DatabaseConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                ticketList.add(new Ticket(
                        rs.getInt("idTicket"),
                        rs.getString("JudulTicket"),
                        rs.getString("DescProbelm"),
                        rs.getString("statusTicket"),
                        rs.getInt("idReporter"),
                        rs.getInt("idTechnician")
                ));
            }
        }catch (SQLException e) {
            System.out.println("Gagal ambil semua ticket: " + e.getMessage());
        }
        return ticketList;
    }

    public void updateTicket(Ticket ticketUpadate) {
        String sql = "UPDATE tickets SET JudulTicket = ?, DescProbelm = ?, statusTicket = ? , idReporter = ? , idTechnician = ? WHERE idTicket = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ticketUpadate.getJudulTicket());
            pstmt.setString(2, ticketUpadate.getDescProbelm());
            pstmt.setString(3, ticketUpadate.getStatusTicket());
            pstmt.setInt(4, ticketUpadate.getIdReporter());
            pstmt.setInt(5, ticketUpadate.getIdTechnician());
            pstmt.setInt(6, ticketUpadate.getIdTicket());
            pstmt.executeUpdate();
            System.out.println("Data Ticket ID " + ticketUpadate.getIdTicket() + " berhasil diperbarui!");
        } catch (SQLException e) {
            System.out.println("Gagal update ticket: " + e.getMessage());
        }
    }

    public void deleteTicket(int idTicket) {
        String sql = "DELETE FROM tickets WHERE idTicket = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idTicket);
            pstmt.executeUpdate();
            System.out.println("ticket ID " + idTicket + " berhasil dihapus.");
        } catch (SQLException e) {
            System.out.println("Gagal hapus ticket: " + e.getMessage());
        }
    }
}