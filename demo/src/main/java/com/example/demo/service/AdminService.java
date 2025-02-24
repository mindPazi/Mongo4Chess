package com.example.demo.service;

import com.example.demo.dao.AdminDAO;
import com.example.demo.model.Match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.model.Admin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final AdminDAO adminDAO;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public AdminService(AdminDAO adminDAO, TournamentService tournamentService,
            PlayerService playerService, MatchService matchService) {
        this.adminDAO = adminDAO;
        this.playerService = playerService;
        this.matchService = matchService;
    }

    public void updateAdminPassword(String oldPassword, String newPassword) {
        String currentUsername = null;
        try {
            // Recupera l'admin autenticato
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            currentUsername = authentication.getName();

            // Recupera l'admin dal DB
            Admin admin = adminDAO.getAdmin(currentUsername);

            if (admin == null) {
                throw new IllegalArgumentException("Admin non trovato.");
            }

            // Verifica la vecchia password
            if (!encoder.matches(oldPassword, admin.getPassword())) {
                throw new IllegalArgumentException("La vecchia password non è corretta.");
            }

            // Codifica la nuova password
            String hashedNewPassword = encoder.encode(newPassword);

            // Aggiorna la password nel DB
            adminDAO.updateAdminPassword(currentUsername, hashedNewPassword);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento della password per l'admin: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nell'aggiornare la password per l'admin.");
        }
    }

    public void updateAdminUsername(String newUsername) {
        // Recupera il nome utente dell'admin autenticato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        try {
            adminDAO.updateAdminUsername(currentUsername, newUsername);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento del nome utente da {} a {}", currentUsername, newUsername, e);
            throw new RuntimeException(
                    "Errore nell'aggiornare il nome utente da " + currentUsername + " a " + newUsername);
        }
    }

    public void deletePlayer(String player) {
        playerService.deletePlayer(player);
    }

    public void saveMatch(Match match) {
        matchService.saveMatch(match);
    }

    public void banPlayer(String player) {
        playerService.banPlayer(player);
    }

    public void unBanPlayer(String player) {
        playerService.unBanPlayer(player);
    }
}
