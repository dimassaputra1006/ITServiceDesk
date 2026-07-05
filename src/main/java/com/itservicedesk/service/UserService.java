package com.itservicedesk.service;

import com.itservicedesk.dao.UserDao;
import com.itservicedesk.model.User;

public class UserService {

    private final UserDao userDao = new UserDao();

    public String addUser(User newUser) {
        if (newUser.getFullName() == null || newUser.getFullName().isBlank()) {
            return "Full name is required.";
        }
        if (newUser.getUsername() == null || newUser.getUsername().isBlank()) {
            return "Username is required.";
        }
        if (userDao.findByUsername(newUser.getUsername()) != null) {
            return "Username is already taken.";
        }
        return userDao.insertUser(newUser) ? null : "Failed to add new user.";
    }

    public String updateProfile(User user, String role, String department) {
        if (role == null || role.isBlank()) {
            return "Role is required.";
        }
        String trimmedDepartment = department != null ? department.trim() : "";
        if (trimmedDepartment.isBlank()) {
            return "Department is required.";
        }

        user.setRole(role);
        user.setDepartment(trimmedDepartment);
        return userDao.updateUser(user) ? null : "Failed to save user changes.";
    }

    public boolean lockUser(String employeeId) {
        return userDao.lockUser(employeeId);
    }

    public boolean unlockUser(String employeeId) {
        return userDao.unlockUser(employeeId);
    }

    public boolean deleteUser(String employeeId) {
        return userDao.deleteUser(employeeId);
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
