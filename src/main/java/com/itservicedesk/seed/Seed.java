package com.itservicedesk.seed;

import com.itservicedesk.model.Ticket;
import com.itservicedesk.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Seed {

    public static void seedTickets() {
        TicketDao ticketDao = new TicketDao();

        // Hapus data lama agar tidak duplikat
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM tickets");
            System.out.println(" Data tickets lama dihapus.");
        } catch (SQLException e) {
            System.out.println("Warning: " + e.getMessage());
        }

        // Dummy Tickets (sesuai dengan model Ticket.java kamu)
        Ticket[] dummyTickets = {
                new Ticket(0, "Laptop tidak bisa booting",
                        "Laptop nyala tapi stuck di logo Windows, sudah dicoba restart beberapa kali.",
                        "Open", 101, 0),

                new Ticket(0, "Email tidak bisa kirim attachment",
                        "Bisa terima email tapi gagal kirim file berukuran besar.",
                        "Open", 102, 0),

                new Ticket(0, "Printer kantor tidak terdeteksi",
                        "Printer tidak muncul di jaringan setelah listrik padam.",
                        "Open", 103, 0),

                new Ticket(0, "Akses shared folder sangat lambat",
                        "Buka folder di server memakan waktu lama.",
                        "Open", 104, 0),

                new Ticket(0, "Monitor flickering setelah update",
                        "Layar berkedip-kedip setelah update Windows terbaru.",
                        "Open", 105, 0)
        };

        for (Ticket t : dummyTickets) {
            ticketDao.insertTicket(t);
        }

        System.out.println(" Berhasil menambahkan 5 dummy ticket!");
    }

    public static void seedUsers() {
        UserDao userDao = new UserDao();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        } catch (SQLException ignored) {}

        // Dummy Users (sesuai dengan model User.java kamu)
        User[] dummyUsers = {
                new User(0, "budi.santoso", "password123", "Staff", "Finance"),
                new User(0, "siti.rahayu", "password123", "Manager", "HR"),
                new User(0, "ahmad.fauzi", "password123", "Staff", "IT"),
                new User(0, "rina.wijaya", "password123", "Supervisor", "Marketing")
        };

        for (User u : dummyUsers) {
            userDao.insertUser(u);
        }

        System.out.println(" Berhasil menambahkan 4 dummy user!");
    }

    public static void main(String[] args) {
        System.out.println("Memulai seeding data...");
        DatabaseConfig.createTablesTickets();
        DatabaseConfig.createTablesUsers();

        seedTickets();
        seedUsers();

        System.out.println("Seeding selesai!");
    }
}