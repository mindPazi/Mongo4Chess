package com.example.demo.service;

import com.example.demo.dao.PlayerDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private final PlayerDAO playerDAO;

    public PlayerService(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    public void banPlayer(String playerUsername) {
        try {
            playerDAO.banPlayer(playerUsername);
        } catch (Exception e) {
            logger.error("Errore durante il ban del giocatore: {}", playerUsername, e);
            throw new RuntimeException("Errore nel bannare il giocatore: " + playerUsername);
        }
    }

    public void unBanPlayer(String playerUsername) {
        try {
            playerDAO.unBanPlayer(playerUsername);
        } catch (Exception e) {
            logger.error("Errore durante lo sblocco del giocatore: {}", playerUsername, e);
            throw new RuntimeException("Errore nel sbloccare il giocatore: " + playerUsername);
        }
    }

    public void deletePlayer(String playerUsername) {
        try {
            playerDAO.deletePlayer(playerUsername);
        } catch (Exception e) {
            logger.error("Errore durante l'eliminazione del giocatore: {}", playerUsername, e);
            throw new RuntimeException("Errore nell'eliminare il giocatore: " + playerUsername);
        }
    }

    public void updatePlayerPassword(String playerUsername, String newPassword) {
        try {
            playerDAO.updatePlayerPassword(playerUsername, newPassword);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento della password per il giocatore: {}", playerUsername, e);
            throw new RuntimeException("Errore nell'aggiornare la password per il giocatore: " + playerUsername);
        }
    }

    public void updatePlayerUsername(String oldUsername, String newUsername) {
        try {
            playerDAO.updatePlayerUsername(oldUsername, newUsername);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento del nome utente da {} a {}", oldUsername, newUsername, e);
            throw new RuntimeException("Errore nell'aggiornare il nome utente da " + oldUsername + " a " + newUsername);
        }
    }

}
