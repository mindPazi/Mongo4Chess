package com.example.demo.service;

import com.example.demo.dao.PlayerDAO;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final PlayerDAO playerDAO;

    public PlayerService(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    public void banPlayer(String playerUsername) {
        playerDAO.banPlayer(playerUsername);
    }

    public void unBanPlayer(String playerUsername) {
        playerDAO.unBanPlayer(playerUsername);
    }

    public void deletePlayer(String playerUsername) {
        playerDAO.deletePlayer(playerUsername);
    }

    public void updatePlayerPassword(String playerUsername, String newPassword) {
        playerDAO.updatePlayerPassword(playerUsername, newPassword);
    }

    public void updatePlayerUsername(String oldUsername, String newUsername) {
        playerDAO.updatePlayerUsername(oldUsername, newUsername);
    }
}
