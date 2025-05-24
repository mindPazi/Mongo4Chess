package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerNodeDAO;
import org.bson.Document;

import com.example.demo.model.Match;
import com.example.demo.dao.PlayerDAO;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

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

    public void saveMatch(Match match) throws Exception {
        if (match.getWhite().equals(match.getBlack())) {
            throw new IllegalArgumentException("Un giocatore non può giocare contro se stesso");
        }

        if (!java.util.List.of("1-0", "0-1", "1/2-1/2").contains(match.getResult())) {
            throw new IllegalArgumentException("Risultato non valido: " + match.getResult());
        }

        java.util.Optional<Integer> whiteEloOpt = playerNodeDAO.getElo(match.getWhite());
        java.util.Optional<Integer> blackEloOpt = playerNodeDAO.getElo(match.getBlack());

        if (whiteEloOpt.isEmpty() || blackEloOpt.isEmpty()) {
            throw new Exception("Uno o entrambi i giocatori non esistono");
        }

        match.setWhiteElo(whiteEloOpt.get());
        match.setBlackElo(blackEloOpt.get());

        java.util.List<Integer> deltaElos = new java.util.ArrayList<>(PlayerService.calculateNewElo(match));
        if (match.getWhiteElo() + deltaElos.get(0) < 0)
            deltaElos.set(0, 0);
        if (match.getBlackElo() + deltaElos.get(1) < 0)
            deltaElos.set(1, 0);

        matchDAO.saveMatch(match);
        playerNodeDAO.setPlayedEdge(match.getWhite(), match.getBlack());

        try {
            switch (match.getResult()) {
                case "1-0":
                    playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 1, 0, 0, 0, 0, 0);
                    playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 0, 0, 1);
                    break;
                case "0-1":
                    playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 0, 0, 1, 0);
                    playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 1, 0, 0, 0, 0);
                    break;
                case "1/2-1/2":
                    playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 1, 0, 0, 0);
                    playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 1, 0, 0);
                    break;
            }

        } catch (Exception e) {
            try {
                switch (match.getResult()) {
                    case "1-0":
                        playerNodeDAO.updatePlayerStats(match.getWhite(), -deltaElos.get(0), -1, 0, 0, 0, 0, 0);
                        playerNodeDAO.updatePlayerStats(match.getBlack(), -deltaElos.get(1), 0, 0, 0, 0, 0, -1);
                        break;
                    case "0-1":
                        playerNodeDAO.updatePlayerStats(match.getWhite(), -deltaElos.get(0), 0, 0, 0, 0, -1, 0);
                        playerNodeDAO.updatePlayerStats(match.getBlack(), -deltaElos.get(1), 0, -1, 0, 0, 0, 0);
                        break;
                    case "1/2-1/2":
                        playerNodeDAO.updatePlayerStats(match.getWhite(), -deltaElos.get(0), 0, 0, -1, 0, 0, 0);
                        playerNodeDAO.updatePlayerStats(match.getBlack(), -deltaElos.get(1), 0, 0, 0, -1, 0, 0);
                        break;
                }
            } catch (Exception neo4jRollbackEx) {
                logger.error("Errore Neo4j: rollback match Mongo eseguito", e);
            }
            deleteMatch(match);
            throw new RuntimeException("Errore Neo4j: rollback match Mongo eseguito", e);
        }
    }

    public void deleteMatch(Match match) {
        try {
            matchDAO.deleteMatch(match);
            playerDAO.removeMatchRef(match.getWhite(), match.getDate());
            playerDAO.removeMatchRef(match.getBlack(), match.getDate());

        } catch (Exception e) {
            try {
                matchDAO.saveMatch(match);
            } catch (Exception rollbackError) {
                throw new RuntimeException("Errore nel rollback del match. Dati potenzialmente incoerenti.",
                        rollbackError);
            }

            throw new RuntimeException("Errore durante la cancellazione del match. Rollback eseguito.", e);
        }
    }

    // public void createMatch(Match match) {
    // matchDAO.saveMatch(match);
    // }

    public void deleteAllMatches() {
        matchDAO.deleteAllMatches();
    }

    public void deleteAllMatchesByPlayer(String player) {
        matchDAO.deleteAllMatchesByPlayer(player);
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

    public void deleteMatchesBeforeDate(Date date) {
        matchDAO.deleteMatchesBeforeDate(date);
    }

    public List<Match> getMatchesByDate(Date startDate, Date endDate) {
        return matchDAO.getMatchesByDate(startDate, endDate);
    }

    public List<Match> getMatchesByElo(int minElo, int maxElo) {
        return matchDAO.getMatchesByElo(minElo, maxElo);
    }
}
