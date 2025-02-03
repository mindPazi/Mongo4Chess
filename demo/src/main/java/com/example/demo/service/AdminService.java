package com.example.demo.service;

import com.example.demo.dao.AdminDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminDAO adminDAO;

    @Autowired
    public AdminService(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    public String updateAdminUsername(String oldUsername, String newUsername) {
        adminDAO.updateAdminUsername(oldUsername, newUsername);
        return "Admin username updated from: " + oldUsername + " to: " + newUsername;
    }

    public String updateAdminPassword(String username, String newPassword) {
        adminDAO.updateAdminPassword(username, newPassword);
        return "Admin password updated successfully for: " + username;
    }
}
