package com.example.demo.service;

import com.example.demo.dao.PlayerDAO;
import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.model.Player;
import com.example.demo.model.PlayerNode;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private final PlayerDAO playerDAO;
    private final PlayerNodeDAO playerNodeDAO;
    @Setter
    private Player player;
    @Setter
    private PlayerNode playerNode;

    public PlayerService(PlayerDAO playerDAO, PlayerNodeDAO playerNodeDAO) {
        this.playerDAO = playerDAO;
        this.playerNodeDAO = playerNodeDAO;
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

    public void getStats() {
        try {
            playerDAO.getStats();
        } catch (Exception e) {
            logger.error("Errore durante il recupero delle statistiche", e);
            throw new RuntimeException("Errore nel recupero delle statistiche");
        }
    }

    public String getEloTrend(String playerId) {
        try {
            return playerDAO.getEloTrend(playerId);
        } catch (Exception e) {
            logger.error("Errore durante il recupero del trend Elo per il giocatore {}", playerId, e);
            throw new RuntimeException("Errore nel recupero del trend Elo per il giocatore " + playerId);
        }
    }

    public void addFriend(String playerId, String friendId) {
        try {
            playerNodeDAO.addFriend(playerId, friendId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta dell'amico {} per il giocatore {}", friendId, playerId, e);
            throw new RuntimeException("Errore nell'aggiungere l'amico " + friendId + " per il giocatore " + playerId);
        }
    }

    public void removeFriend(String playerId, String friendId) {
        try {
            playerNodeDAO.removeFriend(playerId, friendId);
        } catch (Exception e) {
            logger.error("Errore durante la rimozione dell'amico {} per il giocatore {}", friendId, playerId, e);
            throw new RuntimeException(
                    "Errore nella rimozione dell'amico " + friendId + " per il giocatore " + playerId);
        }
    }

    public String createPlayer(String username, String password, int elo) {
        try {
            //playerNodeDAO.createPlayer(username, elo);
            if(playerDAO.getPlayer(username) == null) {
                playerDAO.createPlayer(username, password);
                return "Giocatore creato con successo";
            } else {
                return "Giocatore già esistente";
            }
        } catch (Exception e) {
            logger.error("Errore durante la creazione del giocatore {}", username, e);
            throw new RuntimeException("Errore nella creazione del giocatore " + username);
        }
    }

}
