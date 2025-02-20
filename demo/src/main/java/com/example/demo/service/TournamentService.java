package com.example.demo.service;

import com.example.demo.model.Match;
import com.example.demo.model.Player;
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

    // Metodo per creare un torneo
    public Tournament createTournament(Tournament tournament) {
        return tournamentDAO.createTournament(tournament);
    }

    // Metodo per eliminare un torneo
    public String deleteTournament(String tournamentId) {
        tournamentDAO.deleteTournament(tournamentId);
        return "Tournament deleted successfully: " + tournamentId;
    }

    // Metodo per aggiungere un vincitore al torneo
    public String addWinner(String tournamentId, String winnerUsername) {
        Player winner = new Player();
        winner.setUsername(winnerUsername);

        tournamentDAO.addWinner(tournamentId, winner);
        return "Winner added successfully: " + winnerUsername + " to tournament: " + tournamentId;
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

    public void addMatch(String tournamentId, Match match) {
        try {
            tournamentDAO.addMatch(tournamentId, match);
            logger.info("Match aggiunto con successo al torneo {}", tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta del match al torneo {}", tournamentId, e);
            throw new RuntimeException("Errore nell'aggiungere il match al torneo " + tournamentId);
        }
    }

    public void addPlayer(String tournamentId, String playerId) {
        try {
            tournamentDAO.addPlayer(tournamentId, playerId);
            logger.info("Giocatore {} aggiunto con successo al torneo {}", playerId, tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta del giocatore {} al torneo {}", playerId, tournamentId, e);
            throw new RuntimeException(
                    "Errore nell'aggiungere il giocatore " + playerId + " al torneo " + tournamentId);
        }
    }

    public void removePlayer(String tournamentId, String playerId) {
        try {
            tournamentDAO.removePlayer(tournamentId, playerId);
            logger.info("Giocatore {} rimosso con successo dal torneo {}", playerId, tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante la rimozione del giocatore {} dal torneo {}", playerId, tournamentId, e);
            throw new RuntimeException(
                    "Errore nella rimozione del giocatore " + playerId + " dal torneo " + tournamentId);
        }
    }

}
