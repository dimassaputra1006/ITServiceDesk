package com.itservicedesk.seed;

import com.itservicedesk.config.DatabaseConfig;
import com.itservicedesk.dao.TicketDao;
import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.Ticket;
import com.itservicedesk.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Seed {

    public static void main(String[] args) {
        System.out.println("Memulai Seeding Data ");

        DatabaseConfig.createTables();

        UserDao userDao = new UserDao();
        TicketDao ticketDao = new TicketDao();

        clearExistingData();

        System.out.println("Membuat dummy users");
        User[] users = createDummyUsers();
        String[] userIds = new String[users.length];

        for (int i = 0; i < users.length; i++) {
            if (userDao.insertUser(users[i])) {
                userIds[i] = users[i].getEmployeeId();
                System.out.printf("User dibuat: %s ( %s )\n", users[i].getFullName(), userIds[i]);
            } else {
                System.out.println("GAGAL insert user: " + users[i].getFullName());
            }
        }

        System.out.println("Membuat dummy tickets");
        createDummyTickets(ticketDao, userIds);

        System.out.println("Seeding selesai! Database siap untuk testing.");
    }

    private static void clearExistingData() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM activity_log");
            stmt.execute("DELETE FROM tickets");
            stmt.execute("DELETE FROM users");
            System.out.println("Data lama dihapus.");
        } catch (SQLException e) {
            System.out.println("Warning: " + e.getMessage());
        }
    }

    // Urutan constructor User: fullName, username, email, phone, title, assignedDevice, plainPassword, role, department
    private static User[] createDummyUsers() {
        return new User[]{
                new User("Budi Santoso", "budi.santoso", "budi@company.com", "081234567890",
                        "Finance Officer", "Laptop Dell XPS", "password123", "Staff", "Finance"),

                new User("Siti Rahayu", "siti.rahayu", "siti@company.com", "081298765432",
                        "HR Manager", "MacBook Pro", "password123", "Manager", "HR"),

                new User("Ahmad Fauzi", "ahmad.fauzi", "ahmad@company.com", "085712345678",
                        "IT Support Specialist", "Lenovo ThinkPad", "password123", "Staff", "IT"),

                new User("Rina Wijaya", "rina.wijaya", "rina@company.com", "087812345678",
                        "Marketing Supervisor", "HP Pavilion", "password123", "Supervisor", "Marketing"),

                new User("Dedi Wijaya", "dedi.wijaya", "dedi@company.com", "081987654321",
                        "Graphic Designer", "iMac 24 inch", "password123", "Staff", "Design")
        };
    }

    private static void createDummyTickets(TicketDao ticketDao, String[] userIds) {
        Ticket t1 = new Ticket(
                "[CRITICAL] Sistem SAP Gagal Posting Invoice",
                "Error Code ERR-SAP-992: Database temporary table full saat posting Invoice #INV-2026-0501",
                userIds[0], "Critical");
        ticketDao.insertTicket(t1);

        Ticket t2 = new Ticket(
                "[HIGH] Laptop Dedi - Layar Bergaris dan Flicker",
                "Layar laptop asset IT-772 muncul garis hijau dan flicker parah, sudah direstart tetap bermasalah",
                userIds[4], "High");
        ticketDao.insertTicket(t2);
        ticketDao.claimTicket(t2.getTicketId(), userIds[2]);

        Ticket t3 = new Ticket(
                "[MEDIUM] WiFi Kantor No Internet",
                "Terhubung ke WiFi tapi dapat IP 169.254.x.x (Limited Access)",
                userIds[1], "Medium");
        ticketDao.insertTicket(t3);

        Ticket t4 = new Ticket(
                "[HIGH] Akun Domain Terkunci",
                "Akun Budi terkunci setelah salah password 3x",
                userIds[0], "High");
        ticketDao.insertTicket(t4);
        ticketDao.claimTicket(t4.getTicketId(), userIds[2]);
        ticketDao.resolveTicket(t4.getTicketId());

        Ticket t5 = new Ticket(
                "[LOW] Request Instalasi Adobe Photoshop",
                "Mohon instalasi Adobe Photoshop untuk tim Marketing",
                userIds[3], "Low");
        ticketDao.insertTicket(t5);

        System.out.println("5 ticket berhasil ditambahkan.");
    }
}