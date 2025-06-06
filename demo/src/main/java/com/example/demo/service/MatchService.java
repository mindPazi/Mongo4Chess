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
    private final MongoTransactionService mongoTransactionService;
    private final Neo4jTransactionService neo4jTransactionService;

    public MatchService(MatchDAO matchDAO, PlayerNodeDAO playerNodeDAO, PlayerDAO playerDAO,  MongoTransactionService mongoTransactionService,
    Neo4jTransactionService neo4jTransactionService) {
        this.matchDAO = matchDAO;
        this.playerNodeDAO = playerNodeDAO;
        this.playerDAO = playerDAO;
        this.mongoTransactionService=mongoTransactionService;
        this.neo4jTransactionService=neo4jTransactionService;
    }

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
            mongoTransactionService.persistMatchInMongo(match);
        } catch (Exception e) {
            logger.error("Error saving match. MongoDB transaction cancelled.", e);
            throw new RuntimeException("Error saving match");
        }

        // 5) Neo4j Update ( Neo4j Transaction ) ---
        try {
            neo4jTransactionService.updateNeo4jStatsAndRelations(match, deltaElos);
        } catch (Neo4jException e) {
            // If Neo4j fails, MongoDB compensation
            mongoTransactionService.compensateMongoMatchSave(match);
            logger.error("Error updating Neo4j. Rolling back in MongoDB.", e);
            throw new RuntimeException("Error saving match");
        }

        logger.info("Match successfully saved in MongoDB and Neo4j.");
    }


    // Matches are deleted in both player and match collections.
    // The idea is to lighten the load on the player's document by deleting older matches.
    // If the admin needs to delete all of a player's matches for some reason (perhaps due to a ban),
    // they are kept in the opponents' match list (for their elo trend).
    // Statistics on the graph are kept instead.
    @Transactional("mongoTransactionManager")
    public void deleteAllMatches() {
        matchDAO.deleteAllMatches();
        playerDAO.deleteAllMatches();
    }

    @Transactional("mongoTransactionManager")
    public void deleteAllMatchesByPlayer(String player) {
        matchDAO.deleteAllMatchesByPlayer(player);
        playerDAO.deleteAllMatchesByPlayer(player);
    }

    @Transactional("mongoTransactionManager")
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
