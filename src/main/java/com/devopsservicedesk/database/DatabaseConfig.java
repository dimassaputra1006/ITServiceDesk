package com.devopsservicedesk.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:src/main/resources/database/devOpsServiceDesk.db";

    public static Connection getConnection(){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(URL);
            System.out.println("Koneksi SQLite Berhasil!");
        } catch (SQLException e){
            System.out.println("Koneksi SQLite Gagal: " + e.getMessage());
        }
        return conn;
    }

    public static void createTablesTicket(){
        String sqlticket = "create table if not exists tickets (\n" +
                "    idTicket INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    judulTicket TEXT,\n" +
                "    descProblem TEXT,\n" +
                "    statusTicket TEXT,\n" +
                "    idReporter INTEGER,\n" +
                "    idTechnician INTEGER\n" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()){
            stmt.execute(sqlticket);
            System.out.println("Table ticket telah dibuat!!!");
        } catch (SQLException e){
            System.out.println("Gagal membuat tabel: " + e.getMessage());
        }
    }

    public static void createTablesUser(){
        String sqluser = "create table if not exists users (\n" +
                "    idEmployee INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    username TEXT,\n" +
                "    password TEXT,\n" +
                "    role TEXT,\n" +
                "    department TEXT\n" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()){
            stmt.execute(sqluser);
            System.out.println("Table user telah dibuat!!!");
        } catch (SQLException e){
            System.out.println("Gagal membuat tabel: " + e.getMessage());
        }
    }

    public static void main(String[] args){
        createTablesTicket();
        createTablesUser();
        getConnection();

    }
}
