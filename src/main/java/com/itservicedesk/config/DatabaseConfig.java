package com.itservicedesk.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String DB_PATH = "src/main/resources/database/ITServiceDesk.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection getConnection() {
        File dbDir = new File("src/main/resources/database");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
            System.out.println("Database folder created.");
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("SQLite connection failed: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Deletes the existing database file, if any.
     * Use this only when the schema changes (columns added/removed) — SQLite's
     * "CREATE TABLE IF NOT EXISTS" will NOT add new columns to a file that
     * already exists, so the old file must be removed before createTables()
     * can rebuild it with the new schema. This wipes all data.
     */
    public static void resetDatabase() {
        File dbFile = new File(DB_PATH);
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                System.out.println("Old database file deleted: " + DB_PATH);
            } else {
                System.err.println("Failed to delete old database file: " + DB_PATH);
            }
        }
    }

    public static void createTables() {
        createTablesTickets();
        createTablesUsers();
        createTableActivityLog();
        migrateSchema();
    }

    public static void createTablesTickets() {
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

    public static void createTablesUsers() {
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
                    avatar_path TEXT,
                    is_active BOOLEAN DEFAULT true,
                    is_locked BOOLEAN DEFAULT false,
                    created_at TEXT,
                    updated_at TEXT,
                    last_login_at TEXT
                );
                """;

        executeUpdate(sql, "Table users");
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

    /**
     * Adds columns introduced after the first MVP schema.
     * SQLite's CREATE TABLE IF NOT EXISTS does not alter existing files.
     */
    public static void migrateSchema() {
        addColumnIfMissing("users", "phone", "TEXT");
        addColumnIfMissing("users", "title", "TEXT");
        addColumnIfMissing("users", "assigned_device", "TEXT");
        addColumnIfMissing("users", "avatar_path", "TEXT");
    }

    private static void addColumnIfMissing(String table, String column, String definition) {
        if (hasColumn(table, column)) {
            return;
        }

        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
        executeUpdate(sql, "Column " + table + "." + column);
    }

    private static boolean hasColumn(String table, String column) {
        String sql = "PRAGMA table_info(" + table + ")";
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.printf("Failed to inspect column %s.%s: %s%n", table, column, e.getMessage());
        }
        return false;
    }

    public static void executeUpdate(String sql, String tableName) {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(sql);
            System.out.printf("%s created / already exists%n", tableName);
        } catch (SQLException e) {
            System.err.printf("Failed to create %s: %s%n", tableName, e.getMessage());
        }
    }

    public static void main(String[] args) {
        createTables();
    }
}