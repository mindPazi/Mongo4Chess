package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.model.PlayerMatch;
import org.bson.Document;

import com.example.demo.model.Match;
import com.example.demo.dao.PlayerDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.neo4j.driver.exceptions.Neo4jException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service

public class MatchService {
    private final MatchDAO matchDAO;
    private final PlayerNodeDAO playerNodeDAO;
    private final PlayerDAO playerDAO;
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    public MatchService(MatchDAO matchDAO, PlayerNodeDAO playerNodeDAO, PlayerDAO playerDAO) {
        this.matchDAO = matchDAO;
        this.playerNodeDAO = playerNodeDAO;
        this.playerDAO = playerDAO;
    }

    @Transactional // This will handle rollback/commit for MongoDB
    public void saveMatch(Match match) throws Exception {
        // 1) Basic validations
        if (match.getWhite().equals(match.getBlack())) {
            throw new IllegalArgumentException("A player " +
                                               "cannot play with themselves ! ");
        }
        if (!List.of("1-0", "0-1", "1/2-1/2").contains(match.getResult())) {
            throw new IllegalArgumentException(" Invalid result : " + match.getResult());
        }

        // 2) Getting the actual elo from the node in Neo4j
        Optional<Integer> whiteEloOpt = playerNodeDAO.getElo(match.getWhite());
        Optional<Integer> blackEloOpt = playerNodeDAO.getElo(match.getBlack());
        if (whiteEloOpt.isEmpty() || blackEloOpt.isEmpty()) {
        // This scenario could indicate a synchronization error or that the players were not created correctly .
        logger.error("One or both players do not exist in Neo4j. Cannot proceed.");
        throw new Exception("One or both players do not exist. Cannot proceed.");
        }
        match.setWhiteElo(whiteEloOpt.get());
        match.setBlackElo(blackEloOpt.get());

        // 3) deltaElo calculation (zero clamp)
        List<Integer> deltaElos = new ArrayList<>(PlayerService.calculateNewElo(match));
        if (match.getWhiteElo() + deltaElos.get(0) < 0)
            deltaElos.set(0, 0);
        if (match.getBlackElo() + deltaElos.get(1) < 0)
            deltaElos.set(1, 0);

        // 4) Persistence in MongoDB ( MongoDB Transaction ) ---
        try {
            persistMatchInMongo(match);
        } catch (Exception e) {
            logger.error("Error saving match. MongoDB transaction cancelled.", e);
            throw new RuntimeException("Error saving match");
        }

        // 5) Neo4j Update ( Neo4j Transaction ) ---
        try {
            updateNeo4jStatsAndRelations(match, deltaElos);
        } catch (Neo4jException e) {
            // If Neo4j fails, MongoDB compensation
            compensateMongoMatchSave(match);
            logger.error("Error updating Neo4j. Rolling back in MongoDB.", e);
            throw new RuntimeException("Error saving match");
        }

        logger.info("Match successfully saved in MongoDB and Neo4j.");
    }

    @Transactional//("mongoTransactionManager") // This transaction ensures atomicity of MongoDB operations
    protected void persistMatchInMongo(Match match) {
        matchDAO.saveMatch(match); // Save the Match document
        logger.debug("Match saved in matchDAO.");

        // Adding Matches to Players in MongoDB
        PlayerMatch whitePlayerMatch = new PlayerMatch(match.getWhiteElo(), match.getDate());
        PlayerMatch blackPlayerMatch = new PlayerMatch(match.getBlackElo(), match.getDate());

        playerDAO.addMatch(match.getWhite(), whitePlayerMatch);
        logger.debug("Match added to white player in MongoDB.");
        playerDAO.addMatch(match.getBlack(), blackPlayerMatch);
        logger.debug("Match added to black player in MongoDB.");
    }

    @Transactional
    protected void updateNeo4jStatsAndRelations(Match match, List<Integer> deltaElos) {
        // We use a single Neo4j transaction for all graph operations
        playerNodeDAO.setPlayedEdge(match.getWhite(), match.getBlack());

        // Neo4j statistics update
        switch (match.getResult()) {
            case "1-0":
                playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 1, 0, 0, 0, 0, 0);
                playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 0, 0, 1);
                break;
            case "0-1":
                playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 0, 0, 1, 0);
                playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 1, 0, 0, 0, 0, 1);
                break;
            case "1/2-1/2":
                playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 1, 0, 0, 0);
                playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 1, 0, 0);
                break;
        }
        logger.debug("Statistic updated in Neo4j and PLAYED relation updated.");
    } // Neo4j transaction is automatically committed or rolled back here


    // Compensation method for MongoDB : if Neo4j transaction
    // fails, the rollback on MongoDB is performed.
    @Transactional//("mongoTransactionManager")
    protected void compensateMongoMatchSave(Match match) {
        logger.warn("Start compensation for MongoDB for ID match: {}", match.getId());
        try {
            // Saved match removed
            matchDAO.deleteMatch(match);
            logger.debug("Match with ID {} removed from matchDAO (compensation).", match.getId());

            // Match removed from player's document
            //todo: rimuoverli con l'id del match
            playerDAO.removeMatch(match.getWhite(), match.getDate(), match.getWhiteElo());
            playerDAO.removeMatch(match.getBlack(), match.getDate(), match.getBlackElo());
            logger.debug("Match with ID {} removed from players in MongoDB (compensation).", match.getId());
            logger.info("MongoDB compensation for match ID {} completed.", match.getId());
        } catch (Exception e) {
            logger.error("FATAL ERROR: Failure while clearing MongoDB for match ID {}. Requires manual intervention!", match.getId(), e);
            throw new RuntimeException("Error saving match");
        }
    }

    // Matches are deleted in both player and match collections.
    // The idea is to lighten the load on the player's document by deleting older matches.
    // If the admin needs to delete all of a player's matches for some reason (perhaps due to a ban),
    // they are kept in the opponents' match list (for their elo trend).
    // Statistics on the graph are kept instead.
    @Transactional
    public void deleteAllMatches() {
        matchDAO.deleteAllMatches();
        playerDAO.deleteAllMatches();
    }

    @Transactional
    public void deleteAllMatchesByPlayer(String player) {
        matchDAO.deleteAllMatchesByPlayer(player);
        playerDAO.deleteAllMatchesByPlayer(player);
    }

    @Transactional
    public void deleteMatchesBeforeDate(Date date) {
        matchDAO.deleteMatchesBeforeDate(date);
        playerDAO.deleteMatchesBeforeDate(date);
    }

    public List<Document> getNumOfWinsAndDrawsPerElo(int elomin, int elomax) {
        return matchDAO.getNumOfWinsAndDrawsPerElo(elomin, elomax);
    }

    public List<Match> getMatches() {
        return matchDAO.getMatches();
    }

    public List<Match> getMatchesByPlayer(String player) {
        return matchDAO.getMatchesByPlayer(player);
    }

    public Document getOpeningWithHigherWinRatePerElo(int elomin, int elomax) {
        return matchDAO.getOpeningWithHigherWinRatePerElo(elomin, elomax);
    }

    public List<Document> getMostPlayedOpeningsPerElo(int elomin, int elomax) {
        return matchDAO.getMostPlayedOpeningsPerElo(elomin, elomax);
    }

    public List<Match> getMatchesByDate(Date startDate, Date endDate) {
        return matchDAO.getMatchesByDate(startDate, endDate);
    }

    public List<Match> getMatchesByElo(int minElo, int maxElo) {
        return matchDAO.getMatchesByElo(minElo, maxElo);
    }
}
