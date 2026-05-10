package com.example.devopsservicedesk.models;

public class User {
    private int idEmployee;
    private String username;
    private String password;
    private String role;
    private String department;

    // constructor blank
    public User(){
    }

    //constructor for a new object
    public User(int idEmployee, String username, String password, String role, String department){
        this.idEmployee = idEmployee;
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
    }

    // getter
    public int getidEmployee(){
        return idEmployee;
    }

    // setter
    public void setidEmployee(int idEmployee){
        this.idEmployee = idEmployee;
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getpassword() {
        return password;
    }

    public void setpassword(String password) {
        this.password = password;
    }

    public String getrole() {
        return role;
    }

    public void setrole(String role) {
        this.role = role;
    }

    public String getdepartment() {
        return department;
    }

    public void setdepartment(String department) {
        this.department = department;
    }
}