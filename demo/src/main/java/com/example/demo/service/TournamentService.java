package com.example.demo.service;

import com.example.demo.model.Tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.dao.TournamentDAO;

@Service

public class TournamentService {

    private final TournamentDAO tournamentDAO;

    @Autowired
    public TournamentService(TournamentDAO tournamentDAO) {
        this.tournamentDAO = tournamentDAO;
    }

    public String createTournament(Tournament tournament) {
        return "Tournament created successfully: " + tournament.toString();
    }

    public String deleteTournament(String tournament) {
        return "Tournament deleted successfully: " + tournament;
    }

    public String updateTournament(String tournament) {
        return "Tournament updated successfully: " + tournament;
    }

    public String addWinner(String tournament, String winner) {
        return "Winner added successfully: " + winner + " to tournament: " + tournament;
    }

    public List<Tournament> getAllTournaments() {
        return tournamentDAO.getAllTournaments();
    }

}
