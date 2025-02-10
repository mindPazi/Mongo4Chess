package com.example.demo.service;

import com.example.demo.model.Match;
import com.example.demo.model.Tournament;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.dao.TournamentDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service

public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    private final TournamentDAO tournamentDAO;

    @Autowired
    public TournamentService(TournamentDAO tournamentDAO) {
        this.tournamentDAO = tournamentDAO;
    }

    public String createTournament(Tournament tournament) {
        return "Tournament created successfully: " + tournament.toString();
    }

    public String deleteTournament(Tournament tournament) {
        return "Tournament deleted successfully: " + tournament;
    }

    public String updateTournament(String tournament) {
        return "Tournament updated successfully: " + tournament;
    }

    public String addWinner(String tournament, String winner) {
        return "Winner added successfully: " + winner + " to tournament: " + tournament;
    }

    public List<Tournament> getAllTournaments() {
        try {
            return tournamentDAO.getAllTournaments();
        } catch (Exception e) {
            logger.error("Errore durante il recupero di tutti i tornei", e);
            throw new RuntimeException("Errore nel recupero di tutti i tornei");
        }
    }

    public List<Tournament> getActiveTournaments() {
        try {
            return tournamentDAO.getActiveTournaments();
        } catch (Exception e) {
            logger.error("Errore durante il recupero dei tornei attivi", e);
            throw new RuntimeException("Errore nel recupero dei tornei attivi");
        }
    }

    public void addMostImportantMatches(List<Match> matches, String tournamentId) {
        try {
            tournamentDAO.addMostImportantMatches(matches, tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta delle partite più importanti al torneo {}", tournamentId, e);
            throw new RuntimeException("Errore nell'aggiungere le partite più importanti al torneo " + tournamentId);
        }
    }

    public void joinTournament(String tournamentId, String playerUsername) {
        try {
            tournamentDAO.joinTournament(tournamentId, playerUsername);
        } catch (Exception e) {
            logger.error("Errore durante l'iscrizione al torneo {} da parte del giocatore {}", tournamentId,
                    playerUsername, e);
            throw new RuntimeException(
                    "Errore nell'iscriversi al torneo " + tournamentId + " da parte del giocatore " + playerUsername);
        }
    }

    public String getCreatedTournaments(String creator) {
        return tournamentDAO.getCreatedTournaments(creator);
    }
}
