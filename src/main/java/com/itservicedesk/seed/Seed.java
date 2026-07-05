package com.itservicedesk.seed;

import com.itservicedesk.config.DatabaseConfig;
import com.itservicedesk.dao.TicketDao;
import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.Ticket;
import com.itservicedesk.model.User;

public class Seed {

    public static void main(String[] args) {
        System.out.println("Starting data seeding...");

        // Schema changed (avatar_path column added) — wipe the old .db file
        // so createTables() can rebuild it with the new schema.
        DatabaseConfig.resetDatabase();
        DatabaseConfig.createTables();

        UserDao userDao = new UserDao();
        TicketDao ticketDao = new TicketDao();

        System.out.println("Creating dummy users");
        User[] users = createDummyUsers();
        String[] userIds = new String[users.length];

        for (int i = 0; i < users.length; i++) {
            if (userDao.insertUser(users[i])) {
                userIds[i] = users[i].getEmployeeId();
                System.out.printf("User created: %s (%s)%n", users[i].getFullName(), userIds[i]);
            } else {
                System.out.println("FAILED to insert user: " + users[i].getFullName());
            }
        }

        System.out.println("Creating dummy tickets");
        createDummyTickets(ticketDao, userIds);

        System.out.println("Seeding complete! Database is ready for testing.");
    }

    // Order matches the User constructor: fullName, username, email, phone, title, assignedDevice, plainPassword, role, department
    private static User[] createDummyUsers() {
        return new User[]{
                new User("Budi Santoso", "budi.santoso", "budi@company.com", "081234567890",
                        "Dell XPS Laptop", "password123", "Staff", "Finance"),

                new User("Siti Rahayu", "siti.rahayu", "siti@company.com", "081298765432",
                        "MacBook Pro", "password123", "Manager", "HR"),

                new User("Ahmad Fauzi", "ahmad.fauzi", "ahmad@company.com", "085712345678",
                        "Lenovo ThinkPad", "password123", "Staff", "IT"),

                new User("Rina Wijaya", "rina.wijaya", "rina@company.com", "087812345678",
                        "HP Pavilion", "password123", "Supervisor", "Marketing"),

                new User("Dedi Wijaya", "dedi.wijaya", "dedi@company.com", "081987654321",
                        "24-inch iMac", "password123", "Staff", "Design")
        };
    }

    private static void createDummyTickets(TicketDao ticketDao, String[] userIds) {
        Ticket t1 = new Ticket(
                "SAP System Failed to Post Invoice",
                "Error Code ERR-SAP-992: Database temporary table full while posting Invoice #INV-2026-0501",
                userIds[0], "Critical");
        ticketDao.insertTicket(t1);

        Ticket t2 = new Ticket(
                "Dedi's Laptop - Screen Flickering with Lines",
                "Screen on asset IT-772 shows green lines and severe flickering, still occurs after restart",
                userIds[4], "High");
        ticketDao.insertTicket(t2);
        ticketDao.claimTicket(t2.getTicketId(), userIds[2]);

        Ticket t3 = new Ticket(
                "Office WiFi No Internet",
                "Connected to WiFi but getting a 169.254.x.x IP (Limited Access)",
                userIds[1], "Medium");
        ticketDao.insertTicket(t3);

        Ticket t4 = new Ticket(
                "Domain Account Locked",
                "Budi's account locked after 3 failed password attempts",
                userIds[0], "High");
        ticketDao.insertTicket(t4);
        ticketDao.claimTicket(t4.getTicketId(), userIds[2]);
        ticketDao.resolveTicket(t4.getTicketId());

        Ticket t5 = new Ticket(
                "Adobe Photoshop Installation Request",
                "Please install Adobe Photoshop for the Marketing team",
                userIds[3], "Low");
        ticketDao.insertTicket(t5);

        System.out.println("5 tickets added successfully.");
    }
}