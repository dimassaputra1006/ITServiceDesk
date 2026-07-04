package com.itservicedesk.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:src/main/resources/database/ITServiceDesk.db";

    public static Connection getConnection(){
        java.io.File dbDir = new java.io.File("src/main/resources/database");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
            System.out.println("Folder database dibuat.");
        }
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(URL);
            System.out.println("Koneksi SQLite Berhasil!");
        } catch (SQLException e){
            System.out.println("Koneksi SQLite Gagal: " + e.getMessage());
        }
        return conn;
    }

    public static void createTables() {
        createTablesTickets();
        createTablesUsers();
        createTableActivityLog();
    }

    public static void createTablesTickets(){
        String sql = """
                CREATE TABLE IF NOT EXISTS tickets (
                    ticket_id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT,
                    status TEXT DEFAULT 'Open',
                    priority TEXT DEFAULT 'Medium',
                    reporter_id TEXT,
                    assignee_id TEXT,
                    created_at TEXT,
                    updated_at TEXT,
                    resolved_at TEXT
                );
                """;
        executeUpdate(sql, "Table tickets");
    }

    public static void createTablesUsers(){
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    employee_id TEXT PRIMARY KEY,
                    full_name TEXT,
                    username TEXT UNIQUE,
                    email TEXT,
                    phone TEXT,
                    title TEXT,
                    assigned_device TEXT,
                    password_hash TEXT,
                    role TEXT,
                    department TEXT,
                    is_active BOOLEAN DEFAULT true,
                    is_locked BOOLEAN DEFAULT false,
                    created_at TEXT,
                    updated_at TEXT,
                    last_login_at TEXT
                );
                """;

        executeUpdate(sql, "Table Users");
    }

    public static void createTableActivityLog() {
        String sql = """
                CREATE TABLE IF NOT EXISTS activity_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ticket_id TEXT NOT NULL,
                    analyst_id TEXT,
                    action TEXT NOT NULL,
                    message TEXT,
                    created_at TEXT
                );
                """;
        executeUpdate(sql, "Table activity_log");
    }

    public static void executeUpdate(String sql, String tableName) {
        try (Connection conn = getConnection();
        Statement statement = conn.createStatement()) {
            statement.execute(sql);
            System.out.printf("%s telah dibuat / sudah ada \n", tableName);
        }catch (SQLException e) {   
            System.out.printf("Gagal membuat %s %s", tableName, e.getMessage());
        }
    }

    public static void main(String[] args){
        createTables();
    }
}
