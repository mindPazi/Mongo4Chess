package com.example.demo.service;

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

    @Transactional("mongoTransactionManager")
    public void deleteTournament(String tournamentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Tournament tournament = tournamentDAO.getTournament(tournamentId);
        // if you are not the creator and you are not an admin you cannot delete a tournament
        if (!Objects.equals(authentication.getName(), tournament.getCreator())
                && authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("You are not the creator of the tournament or do not have administrator privileges.");
        }
        tournamentDAO.deleteTournament(tournamentId);
        for (TournamentPlayer player : tournament.getPlayers()) {
            playerDAO.removeTournament(tournamentId, player.getUsername());
        }
    }

    public List<Tournament> getAllTournaments() {
        try {
            return tournamentDAO.getAllTournaments();
        } catch (Exception e) {
            logger.error("Error while retrieving all tournaments", e);
            throw new RuntimeException("Error while retrieving all tournaments");
        }
    }

    public List<Tournament> getAvailableTournaments() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        PlayerNode player = playerNodeDAO.getPlayer(username);
        int playerElo = player.getElo();

        return tournamentDAO.getAvailableTournaments(playerElo);
    }

    @Transactional("mongoTransactionManager")
    public void joinTournament(String tournamentId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String playerUsername = authentication.getName();

        addPlayerToTournament(tournamentId, playerUsername);
    }

    public List<Tournament> getCreatedTournaments(String creator) {
        return tournamentDAO.getCreatedTournaments(creator);
    }

    // used by admin
    @Transactional("mongoTransactionManager")
    public void addPlayerByAdmin(String tournamentId, String playerId) throws RuntimeException {
        if (playerDAO.getPlayer(playerId) == null) {
            throw new RuntimeException("Player not found");
        }
        addPlayerToTournament(tournamentId, playerId);
    }

    // l'annotazione @Transactional è nelle funzioni chiamanti, se si mette anche qui si rompe
    public void addPlayerToTournament(String tournamentId, String playerId) {
        PlayerNode playerNode = playerNodeDAO.getPlayerById(playerId);
        Tournament tournament = tournamentDAO.getTournament(tournamentId);

        if (playerNode.getElo() < tournament.getEloMin() || playerNode.getElo() > tournament.getEloMax()) {
            throw new RuntimeException("Invalid Elo.");
        }

        if (tournament.getIsClosed()) {
            throw new RuntimeException("Tournament is closed.");
        }
        if (tournament.getPlayers().stream().anyMatch(p -> p.getUsername().equals(playerId))) {
            throw new RuntimeException("Plyer already subscribed.");
        }
        if (tournament.getPlayers().size() >= tournament.getMaxPlayers()) {
            throw new RuntimeException("Tournament is full.");
        }

        TournamentPlayer playerEntry = new TournamentPlayer(playerId, 0);
        tournamentDAO.addPlayer(tournamentId, playerEntry);
        playerDAO.addTournament(playerId, new PlayerTournament(tournamentId, tournament.getName(),
                tournament.getStartDate(), tournament.getEndDate(), 0));

        if (tournament.getPlayers().size() + 1 == tournament.getMaxPlayers()) {
            tournamentDAO.closeTournament(tournamentId);
        }
    }

    @Transactional("mongoTransactionManager")
    public void removePlayer(String tournamentId, String playerId) {
        // if the tournament has already started I cannot remove the player
        if (new Date().compareTo(tournamentDAO.getTournament(tournamentId).getStartDate()) > 0) {
            throw new RuntimeException("Tournament has already started, cannot remove player");
        }
        tournamentDAO.removePlayer(tournamentId, playerId);
        tournamentDAO.openTournament(tournamentId);
        playerDAO.removeTournament(tournamentId, playerId);
    }

    @Transactional("mongoTransactionManager")
    public void updatePositions(List<TournamentPlayer> tournamentPlayers, String tournamentId) {
        Tournament tournament = tournamentDAO.getTournament(tournamentId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String creator = tournament.getCreator();

        if (!Objects.equals(authentication.getName(), creator))
            throw new RuntimeException("You are not the creator.");

        if (!tournament.getIsClosed())
            throw new RuntimeException("The tournament is not over yet.");

        tournamentDAO.updatePositions(tournamentPlayers, tournamentId);
        playerDAO.updateTournamentPositions(tournamentPlayers, tournamentId);
        logger.info("Positions updated for the tournament: {}", tournamentId);
    }

    // matches played in tournaments do not affect the players' elo history, nor are
    // they automatically inserted into the matches collection
    public void addMostImportantMatches(String tournamentId, List<TournamentMatch> matches) {
        try {
            Tournament tournament = tournamentDAO.getTournament(tournamentId);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(!Objects.equals(tournament.getCreator(), authentication.getName()) && authentication.getAuthorities().
                    stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) )
                throw new Exception("You have not the privileges to add the matches.");

            // update the elo of white and black before the game and then add the game
            for (TournamentMatch tm : matches) {
                PlayerNode white = playerNodeDAO.getPlayer(tm.getMatch().getWhite());
                PlayerNode black = playerNodeDAO.getPlayer(tm.getMatch().getBlack());
                tm.getMatch().setWhiteElo(white.getElo());
                tm.getMatch().setBlackElo(black.getElo());
            }
            tournamentDAO.addMatchToTournament(tournamentId, matches);
            logger.info("Matches added succesfully to the tournament: {}", tournamentId);
        } catch (Exception e) {
            logger.error("Error adding matches to tournament {}", tournamentId, e);
            throw new RuntimeException("Error adding matches to tournament " + tournamentId);
        }
    }

    public List<Tournament> getTournamentsByDate(Date startDate, Date endDate) {
        try {
            return tournamentDAO.getTournamentsByDate(startDate, endDate);
        } catch (Exception e) {
            logger.error("Error retrieving tournaments between dates {} and {}", startDate, endDate, e);
            throw new RuntimeException("Error retrieving tournaments between dates " + startDate + " and " + endDate);
        }
    }
}
