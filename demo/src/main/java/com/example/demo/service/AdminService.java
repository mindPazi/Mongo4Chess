package com.example.demo.service;

import com.example.demo.dao.AdminDAO;
import com.example.demo.dao.TournamentDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.model.Admin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final AdminDAO adminDAO;
    private final PlayerService playerService;
    private final TournamentDAO tournamentDAO;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public AdminService(AdminDAO adminDAO, TournamentDAO tournamentDAO,
                        PlayerService playerService) {
        this.adminDAO = adminDAO;
        this.playerService = playerService;
        this.tournamentDAO = tournamentDAO;
    }

    public void updateAdminPassword(String oldPassword, String newPassword) {
        String currentUsername = null;
        try {
            // Get the admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            currentUsername = authentication.getName();

            // Get the admin from the DB
            Admin admin = adminDAO.getAdmin(currentUsername);

            if (admin == null) {
                throw new IllegalArgumentException("Admin not found.");
            }

            // Verify the old password
            if (!encoder.matches(oldPassword, admin.getPassword())) {
                throw new IllegalArgumentException("The old password is incorrect.");
            }

            // Codify the new password
            String hashedNewPassword = encoder.encode(newPassword);

            // Update the password in the DB
            adminDAO.updateAdminPassword(currentUsername, hashedNewPassword);

        } catch (Exception e) {
            logger.error("Error updating the password for the admin: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating the password.");
        }
    }

    @Transactional("mongoTransactionManager")
    public void updateAdminUsername(String newUsername) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        try {
            adminDAO.updateAdminUsername(currentUsername, newUsername);
            tournamentDAO.updateCreator(currentUsername, newUsername);
        } catch (Exception e) {
            logger.error("Error updating the username from {} to {}", currentUsername, newUsername, e);
            throw new RuntimeException(
                    "Error updating the username from " + currentUsername + " to " + newUsername);
        }
    }
}
