package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.model.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public TournamentService(TournamentDAO tournamentDAO, PlayerDAO playerDAO,
            PlayerNodeDAO playerNodeDAO) {
        this.tournamentDAO = tournamentDAO;
        this.playerDAO = playerDAO;
        this.playerNodeDAO = playerNodeDAO;
    }

    public Tournament createTournament(Tournament tournament) throws RuntimeException {
        return tournamentDAO.createTournament(tournament);
    }

    @Transactional
    public void deleteTournament(String tournamentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // se non sei il creatore e non sei un admin non puoi eliminare un torneo
        if (!Objects.equals(authentication.getName(), tournamentDAO.getTournament(tournamentId).getCreator())
                && authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Non sei il creatore del torneo o non hai i privilegi amministratore.");
        }
        tournamentDAO.deleteTournament(tournamentId);
        for (TournamentPlayer player : tournamentDAO.getTournament(tournamentId).getPlayers()) {
            playerDAO.removeTournament(tournamentId, player.getUsername());
        }
    }

    public List<Tournament> getAllTournaments() {
        try {
            return tournamentDAO.getAllTournaments();
        } catch (Exception e) {
            logger.error("Errore durante il recupero di tutti i tornei", e);
            throw new RuntimeException("Errore nel recupero di tutti i tornei");
        }
    }

    public List<Tournament> getAvailableTournaments() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        PlayerNode player = playerNodeDAO.getPlayer(username);
        int playerElo = player.getElo();

        return tournamentDAO.getAvailableTournaments(playerElo);
    }

    @Transactional
    public void joinTournament(String tournamentId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String playerUsername = authentication.getName();

        addPlayerToTournament(tournamentId, playerUsername);
    }

    public List<Tournament> getCreatedTournaments(String creator) {
        return tournamentDAO.getCreatedTournaments(creator);
    }

    // used by admin
    @Transactional
    public void addPlayerByAdmin(String tournamentId, String playerId) throws RuntimeException {
        if (playerDAO.getPlayer(playerId) == null) {
            throw new RuntimeException("Player not found");
        }

        addPlayerToTournament(tournamentId, playerId);

        logger.info("Giocatore {} aggiunto con successo al torneo {}", playerId, tournamentId);
    }

    // l'annotazione @Transactional è nelle funzioni chiamanti, se si mette anche qui si rompe
    public void addPlayerToTournament(String tournamentId, String playerId) {
        PlayerNode playerNode = playerNodeDAO.getPlayerById(playerId);
        Tournament tournament = tournamentDAO.getTournament(tournamentId);

        if (playerNode.getElo() < tournament.getEloMin() || playerNode.getElo() > tournament.getEloMax()) {
            throw new RuntimeException("Elo non valido");
        }

        if (tournament.getIsClosed()) {
            throw new RuntimeException("Torneo chiuso");
        }
        if (tournament.getPlayers().stream().anyMatch(p -> p.getUsername().equals(playerId))) {
            throw new RuntimeException("Giocatore già iscritto");
        }
        if (tournament.getPlayers().size() >= tournament.getMaxPlayers()) {
            throw new RuntimeException("Torneo pieno");
        }

        TournamentPlayer playerEntry = new TournamentPlayer(playerId, 0);
        tournamentDAO.addPlayer(tournamentId, playerEntry);
        playerDAO.addTournament(playerId, new PlayerTournament(tournamentId, tournament.getName(),
                tournament.getStartDate(), tournament.getEndDate(), 0));

        if (tournament.getPlayers().size() + 1 == tournament.getMaxPlayers()) {
            tournamentDAO.closeTournament(tournamentId);
        }
    }

    @Transactional
    public void removePlayer(String tournamentId, String playerId) {
        // se il torneo è già iniziato non posso rimuovere il giocatore
        if (new Date().compareTo(tournamentDAO.getTournament(tournamentId).getStartDate()) > 0) {
            throw new RuntimeException("Tournament has already started, cannot remove player");
        }
        tournamentDAO.removePlayer(tournamentId, playerId);
        tournamentDAO.openTournament(tournamentId);
        playerDAO.removeTournament(tournamentId, playerId);

        logger.info("Giocatore {} rimosso con successo dal torneo {}", playerId, tournamentId);
    }

    @Transactional
    public void updatePositions(List<TournamentPlayer> tournamentPlayers, String tournamentId) {
        Tournament tournament = tournamentDAO.getTournament(tournamentId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String creator = tournament.getCreator();

        if (!Objects.equals(authentication.getName(), creator))
            throw new RuntimeException("Non sei il creatore del torneo");

        if (!tournament.getIsClosed())
            throw new RuntimeException("Torneo ancora non chiuso");

        tournamentDAO.updatePositions(tournamentPlayers, tournamentId);
        playerDAO.updateTournamentPositions(tournamentPlayers, tournamentId);
        logger.info("Posizioni aggiornate con successo per il torneo {}", tournamentId);
    }

    // le partite fatte nei tornei non incidono sullo storico elo dei giocatori, nè
    // vengono automaticamente inserite nella collection di matches
    public void addMostImportantMatches(String tournamentId, List<TournamentMatch> matches) {
        try {
            // aggiorna l'elo del bianco e del nero prima della partita e poi aggiunge la
            // partita
            for (TournamentMatch tm : matches) {
                PlayerNode white = playerNodeDAO.getPlayer(tm.getMatch().getWhite());
                PlayerNode black = playerNodeDAO.getPlayer(tm.getMatch().getBlack());
                tm.getMatch().setWhiteElo(white.getElo());
                tm.getMatch().setBlackElo(black.getElo());
            }
            tournamentDAO.addMatchToTournament(tournamentId, matches);
            logger.info("Partite aggiunte con successo al torneo {}", tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta delle partite al torneo {}", tournamentId, e);
            throw new RuntimeException("Errore nell'aggiungere le partite al torneo " + tournamentId);
        }
    }

    public List<Tournament> getTournamentsByDate(Date startDate, Date endDate) {
        try {
            return tournamentDAO.getTournamentsByDate(startDate, endDate);
        } catch (Exception e) {
            logger.error("Errore durante il recupero dei tornei tra le date {} e {}", startDate, endDate, e);
            throw new RuntimeException("Errore nel recupero dei tornei tra le date " + startDate + " e " + endDate);
        }
    }
}
