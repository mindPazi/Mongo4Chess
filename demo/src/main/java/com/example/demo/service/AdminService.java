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

    public String updateAdminUsername(String newUsername) {
        adminDAO.updateAdminUsername(newUsername);
        return "Admin username updated to: " + newUsername;
    }

    public String updateAdminPassword(String newPassword) {
        adminDAO.updateAdminPassword(newPassword);
        return "Admin password updated successfully.";
    }
}
