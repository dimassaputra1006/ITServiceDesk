package com.devopsservicedesk.database;

import com.devopsservicedesk.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao{
    //Create(insert)
    public void insertUser(User penggunaBaru){
        String sql = "INSERT INTO user (username, password, role, department) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, penggunaBaru.getusername());
            pstmt.setString(2, penggunaBaru.getpassword());
            pstmt.setString(3, penggunaBaru.getrole());
            pstmt.setString(4, penggunaBaru.getdepartment());

            pstmt.executeUpdate();
            System.out.println("data pengguna : " + penggunaBaru.getusername() + " berhasil ditambahkan");
        } catch (SQLException e){
            System.out.println("gagal menambahkan data: " + e.getMessage());
        }
    }

    // Read with statement where
    public User getUserByUsername(String username){
        String sql = "select * from user where username = ?";
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                return  new User(
                        rs.getInt("idEmployee"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("department")
                );
            }
        }catch (SQLException e){
            System.out.println("gagal mencari user: "+ e.getMessage());
        }
        return null;
    }

    //Read All
    public List<User> getAllUser(){
        List<User> userList = new ArrayList<>();
        String sql = "Select * from user";
        try(Connection conn = DatabaseConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            while(rs.next()){
                userList.add(new User(
                        rs.getInt("idEmployee"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("department")
                ));
            }
        }catch (SQLException e) {
            System.out.println("Gagal ambil semua user: " + e.getMessage());
        }
        return userList;
    }

    // Update
    public void updateUser(User user) {
        String sql = "UPDATE user SET username = ?, password = ?, role = ?, department = ? WHERE idEmployee = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getusername());
            pstmt.setString(2, user.getpassword());
            pstmt.setString(3, user.getrole());
            pstmt.setString(4, user.getdepartment());
            pstmt.setInt(5, user.getidEmployee());
            pstmt.executeUpdate();
            System.out.println("Data user ID " + user.getidEmployee() + " berhasil diperbarui!");
        } catch (SQLException e) {
            System.out.println("Gagal update user: " + e.getMessage());
        }
    }

    // Delete
    public void deleteUser(int idEmployee) {
        String sql = "DELETE FROM user WHERE idEmployee = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEmployee);
            pstmt.executeUpdate();
            System.out.println("User ID " + idEmployee + " berhasil dihapus.");
        } catch (SQLException e) {
            System.out.println("Gagal hapus user: " + e.getMessage());
        }
    }
}