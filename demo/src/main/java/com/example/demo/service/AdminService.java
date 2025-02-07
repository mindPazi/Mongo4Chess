package com.example.demo.service;

import com.example.demo.dao.AdminDAO;
import com.example.demo.model.Tournament;
import com.example.demo.model.Match;
import com.example.demo.service.TournamentService;
import com.example.demo.service.PlayerService;
import com.example.demo.service.MatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminDAO adminDAO;
    private final TournamentService tournamentService;
    private final PlayerService playerService;
    private final MatchService matchService;

    @Autowired
    public AdminService(AdminDAO adminDAO, TournamentService tournamentService,
            PlayerService playerService, MatchService matchService) {
        this.adminDAO = adminDAO;
        this.tournamentService = tournamentService;
        this.playerService = playerService;
        this.matchService = matchService;
    }

    public String updateAdminUsername(String oldUsername, String newUsername) {
        adminDAO.updateAdminUsername(oldUsername, newUsername);
        return "Admin username updated from: " + oldUsername + " to: " + newUsername;
    }

    public String updateAdminPassword(String username, String newPassword) {
        adminDAO.updateAdminPassword(username, newPassword);
        return "Admin password updated successfully for: " + username;
    }

    public String createTournament(Tournament tournament) {
        return tournamentService.createTournament(tournament);
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
