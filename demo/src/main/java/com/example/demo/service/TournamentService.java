package com.example.demo.service;

import com.example.demo.model.Match;
import com.example.demo.model.Player;
import com.example.demo.model.Tournament;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.example.demo.dao.TournamentDAO;
import com.example.demo.dao.PlayerDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service

public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    private final TournamentDAO tournamentDAO;
    private final PlayerDAO playerDAO;

    @Autowired
    public TournamentService(TournamentDAO tournamentDAO, PlayerDAO playerDAO) {
        this.tournamentDAO = tournamentDAO;
        this.playerDAO = playerDAO;
    }

    public Tournament createTournament(Tournament tournament) {
        return tournamentDAO.createTournament(tournament);
    }

    public String deleteTournament(String tournamentId) {
        tournamentDAO.deleteTournament(tournamentId);
        return "Tournament deleted successfully: " + tournamentId;
    }

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Player player = playerDAO.getPlayer(username);
        int playerElo = player.getElo();

        List<Tournament> activeTournaments = tournamentDAO.getActiveTournaments();

        return activeTournaments.stream()
                .filter(tournament -> !Boolean.TRUE.equals(tournament.getIsClosed()))
                .filter(tournament -> playerElo >= tournament.getEloMin() && playerElo <= tournament.getEloMax())
                .collect(Collectors.toList());
    }

    public void addMostImportantMatches(List<Match> matches, String tournamentId) {
        try {
            tournamentDAO.addMostImportantMatches(matches, tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta delle partite più importanti al torneo {}", tournamentId, e);
            throw new RuntimeException("Errore nell'aggiungere le partite più importanti al torneo " + tournamentId);
        }
    }

    public void joinTournament(String tournamentId) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerUsername = authentication.getName();

            tournamentDAO.joinTournament(tournamentId, playerUsername);
        } catch (Exception e) {
            logger.error("Errore durante l'iscrizione al torneo {} da parte del giocatore {}", tournamentId,
                    e.getMessage(), e);
            throw new RuntimeException("Errore nell'iscriversi al torneo " + tournamentId);
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
