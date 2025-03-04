package com.example.demo.service;

import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.model.Match;
import com.example.demo.model.Player;
import com.example.demo.model.PlayerNode;
import com.example.demo.model.Tournament;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final PlayerNodeDAO playerNodeDAO;

    @Autowired
    public TournamentService(TournamentDAO tournamentDAO, PlayerDAO playerDAO, PlayerNodeDAO playerNodeDAO) {
        this.tournamentDAO = tournamentDAO;
        this.playerDAO = playerDAO;
        this.playerNodeDAO = playerNodeDAO;
    }

    public Tournament createTournament(Tournament tournament) throws RuntimeException {
        return tournamentDAO.createTournament(tournament);
    }

    public void deleteTournament(String tournamentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!Objects.equals(authentication.getName(), tournamentDAO.getTournament(tournamentId).getCreator())) {
            throw new RuntimeException("Non sei il creatore del torneo");
        }
        tournamentDAO.deleteTournament(tournamentId);
    }

    public void addWinner(String tournamentId, String winnerUsername) throws RuntimeException {
        if (playerDAO.getPlayer(winnerUsername) == null) {
            throw new RuntimeException("Player not found");
        }
        tournamentDAO.addWinner(tournamentId, winnerUsername);
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

        PlayerNode player = playerNodeDAO.getPlayer(username);
        int playerElo = player.getElo();

        return tournamentDAO.getActiveTournaments(playerElo);
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

    public List<Tournament> getCreatedTournaments(String creator) {
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

    public void addPlayer(String tournamentId, String playerId) throws RuntimeException {
            if(playerDAO.getPlayer(playerId) == null) {
                throw new RuntimeException("Player not found");
            }

            Tournament tournament = tournamentDAO.getTournament(tournamentId);

            if(tournament.getIsClosed()) {
                throw new RuntimeException("Torneo chiuso");
            }
            if(tournament.getPlayers().stream().anyMatch(playerMap -> playerMap.containsKey(playerId))) {
                throw new RuntimeException("Giocatore già iscritto");
            }
            if(tournament.getPlayers().size()>=tournament.getMaxPlayers()) {
                throw new RuntimeException("Torneo pieno");
            }

            Map<String, Integer> playerEntry = new HashMap<>();
            playerEntry.put(playerId, 0);
            tournamentDAO.addPlayer(tournamentId, playerEntry);

            if(tournament.getPlayers().size() == tournament.getMaxPlayers()) {
                tournamentDAO.closeTournament(tournamentId);
            }

            logger.info("Giocatore {} aggiunto con successo al torneo {}", playerId, tournamentId);
    }

    public void removePlayer(String tournamentId, String playerId) {
        try {
            //todo: capire perchè non rimuove il giocatore correttamente
            tournamentDAO.removePlayer(tournamentId, playerId);
            tournamentDAO.openTournament(tournamentId);
            logger.info("Giocatore {} rimosso con successo dal torneo {}", playerId, tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante la rimozione del giocatore {} dal torneo {}", playerId, tournamentId, e);
            throw new RuntimeException(
                    "Errore nella rimozione del giocatore " + playerId + " dal torneo " + tournamentId);
        }
    }

}
