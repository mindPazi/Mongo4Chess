package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.model.*;

import java.util.List;
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

//todo: aggiungere delete tournament
public class TournamentService {

    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);

    private final TournamentDAO tournamentDAO;
    private final PlayerDAO playerDAO;
    private final PlayerNodeDAO playerNodeDAO;
    private final MatchDAO matchDAO;

    @Autowired
    public TournamentService(MatchDAO matchDAO, TournamentDAO tournamentDAO, PlayerDAO playerDAO, PlayerNodeDAO playerNodeDAO) {
        this.tournamentDAO = tournamentDAO;
        this.playerDAO = playerDAO;
        this.playerNodeDAO = playerNodeDAO;
        this.matchDAO = matchDAO;
    }

    public Tournament createTournament(Tournament tournament) throws RuntimeException {
        return tournamentDAO.createTournament(tournament);
    }

    public void deleteTournament(String tournamentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!Objects.equals(authentication.getName(), tournamentDAO.getTournament(tournamentId).getCreator())) {
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

//    public void addMostImportantMatches(List<Match> matches, String tournamentId) {
//        try {
//            tournamentDAO.addMostImportantMatches(matches, tournamentId);
//        } catch (Exception e) {
//            logger.error("Errore durante l'aggiunta delle partite più importanti al torneo {}", tournamentId, e);
//            throw new RuntimeException("Errore nell'aggiungere le partite più importanti al torneo " + tournamentId);
//        }
//    }

    public void joinTournament(String tournamentId) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerUsername = authentication.getName();

            PlayerNode playerNode = playerNodeDAO.getPlayerById(playerUsername);
            Tournament tournament = tournamentDAO.getTournament(tournamentId);

            if (playerNode.getElo() < tournament.getEloMin() || playerNode.getElo() > tournament.getEloMax()) {
                throw new RuntimeException("Elo non valido");
            }

            if (tournament.getIsClosed()) {
                throw new RuntimeException("Torneo chiuso");
            }
            if (tournament.getPlayers().stream().anyMatch(p -> p.getUsername().equals(playerUsername))) {
                throw new RuntimeException("Giocatore già iscritto");
            }
            if (tournament.getPlayers().size() >= tournament.getMaxPlayers()) {
                throw new RuntimeException("Torneo pieno");
            }

            TournamentPlayer playerEntry = new TournamentPlayer(playerUsername, 0);
            tournamentDAO.addPlayer(tournamentId, playerEntry);
            playerDAO.addTournament(playerUsername, new PlayerTournament(tournamentId, tournament.getName(), tournament.getStartDate(), tournament.getEndDate(), 0));

            if (tournament.getPlayers().size() + 1 == tournament.getMaxPlayers()) {
                tournamentDAO.closeTournament(tournamentId);
            }
        } catch (Exception e) {
            logger.error("Errore durante l'iscrizione al torneo {} da parte del giocatore {}", tournamentId,
                    e.getMessage(), e);
            throw new RuntimeException("Errore nell'iscriversi al torneo " + tournamentId);
        }
    }

    public List<Tournament> getCreatedTournaments(String creator) {
        return tournamentDAO.getCreatedTournaments(creator);
    }

//    public void addMatch(String tournamentId, Match match) {
//        try {
//            tournamentDAO.addMatch(tournamentId, match);
//            logger.info("Match aggiunto con successo al torneo {}", tournamentId);
//        } catch (Exception e) {
//            logger.error("Errore durante l'aggiunta del match al torneo {}", tournamentId, e);
//            throw new RuntimeException("Errore nell'aggiungere il match al torneo " + tournamentId);
//        }
//    }

    // used by admin
    public void addPlayer(String tournamentId, String playerId) throws RuntimeException {
        if (playerDAO.getPlayer(playerId) == null) {
            throw new RuntimeException("Player not found");
        }

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
        playerDAO.addTournament(playerId, new PlayerTournament(tournamentId, tournament.getName(), tournament.getStartDate(), tournament.getEndDate(), 0));

        if (tournament.getPlayers().size() + 1 == tournament.getMaxPlayers()) {
            tournamentDAO.closeTournament(tournamentId);
        }

        logger.info("Giocatore {} aggiunto con successo al torneo {}", playerId, tournamentId);
    }

    public void removePlayer(String tournamentId, String playerId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String creator = tournamentDAO.getTournament(tournamentId).getCreator();

            if (authentication.getName().equals(creator))
                throw new RuntimeException("Non sei il creatore del torneo");

            tournamentDAO.removePlayer(tournamentId, playerId);
            tournamentDAO.openTournament(tournamentId);
            playerDAO.removeTournament(tournamentId, playerId);

            logger.info("Giocatore {} rimosso con successo dal torneo {}", playerId, tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante la rimozione del giocatore {} dal torneo {}", playerId, tournamentId, e);
            throw new RuntimeException(
                    "Errore nella rimozione del giocatore " + playerId + " dal torneo " + tournamentId);
        }
    }

    public void updatePositions(List<TournamentPlayer> tournamentPlayers, String tournamentId) {
        try {
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
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento delle posizioni per il torneo {}", tournamentId, e);
            throw new RuntimeException("Errore nell'aggiornare le posizioni per il torneo " + tournamentId);
        }
    }

    // le partite fatte nei tornei non incidono sullo storico elo dei giocatori, nè vengono automaticamente inserite nella collection di matches
    public void addMostImportantMatches(String tournamentId, List<TournamentMatch> matches) {
        try {
            //aggiorna l'elo del bianco e del nero prima della partita e poi aggiunge la partita
            for (TournamentMatch tm : matches) {
                PlayerNode white = playerNodeDAO.getPlayer(tm.getMatch().getWhite());
                PlayerNode black = playerNodeDAO.getPlayer(tm.getMatch().getBlack());
                tm.getMatch().setWhiteElo(white.getElo());
                tm.getMatch().setBlackElo(black.getElo());
            }
            tournamentDAO.addMatchToTournament(tournamentId, matches);
            //matchDAO.addMatches(matches.stream().map(TournamentMatch::getMatch).collect(Collectors.toList()));
            logger.info("Partite aggiunte con successo al torneo {}", tournamentId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta delle partite al torneo {}", tournamentId, e);
            throw new RuntimeException("Errore nell'aggiungere le partite al torneo " + tournamentId);
        }
    }
}
