package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerNodeDAO;
import org.bson.Document;

import com.example.demo.model.Match;
import com.example.demo.dao.PlayerDAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Transactional
    public void saveMatch(Match match) throws Exception {
        // 1) validazioni base
        if (match.getWhite().equals(match.getBlack())) {
            throw new IllegalArgumentException("Un giocatore non può giocare contro se stesso");
        }
        if (!List.of("1-0", "0-1", "1/2-1/2").contains(match.getResult())) {
            throw new IllegalArgumentException("Risultato non valido: " + match.getResult());
        }

        // 2) recupero Elo attuale da Neo4j
        Optional<Integer> whiteEloOpt = playerNodeDAO.getElo(match.getWhite());
        Optional<Integer> blackEloOpt = playerNodeDAO.getElo(match.getBlack());
        if (whiteEloOpt.isEmpty() || blackEloOpt.isEmpty()) {
            throw new Exception("Uno o entrambi i giocatori non esistono");
        }
        match.setWhiteElo(whiteEloOpt.get());
        match.setBlackElo(blackEloOpt.get());

        // 3) calcolo deltaElo (clamp a zero)
        List<Integer> deltaElos = new ArrayList<>(PlayerService.calculateNewElo(match));
        if (match.getWhiteElo() + deltaElos.get(0) < 0)
            deltaElos.set(0, 0);
        if (match.getBlackElo() + deltaElos.get(1) < 0)
            deltaElos.set(1, 0);

        // 4) persistenza in MongoDB
        matchDAO.saveMatch(match);
        // 5) relazione "played" in Neo4j
        playerNodeDAO.setPlayedEdge(match.getWhite(), match.getBlack());

        // 6) aggiornamento statistiche Neo4j, con rollback manuale controllato
        boolean whiteStatsUpdated = false;
        boolean blackStatsUpdated = false;

        try {
            switch (match.getResult()) {
                case "1-0":
                    playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 1, 0, 0, 0, 0, 0);
                    whiteStatsUpdated = true;
                    playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 0, 0, 1);
                    blackStatsUpdated = true;
                    break;
                case "0-1":
                    playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 0, 0, 1, 0);
                    whiteStatsUpdated = true;
                    playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 1, 0, 0, 0, 0);
                    blackStatsUpdated = true;
                    break;
                case "1/2-1/2":
                    playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 1, 0, 0, 0);
                    whiteStatsUpdated = true;
                    playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 1, 0, 0);
                    blackStatsUpdated = true;
                    break;
            }
        } catch (Exception e) {
            // rollback manuale solo per le stats già aggiornate
            try {
                if (whiteStatsUpdated) {
                    playerNodeDAO.updatePlayerStats(
                            match.getWhite(),
                            -deltaElos.get(0),
                            match.getResult().equals("1-0") ? -1 : match.getResult().equals("1/2-1/2") ? -1 : 0,
                            0, 0, 0, 0, 0);
                }
                if (blackStatsUpdated) {
                    playerNodeDAO.updatePlayerStats(
                            match.getBlack(),
                            -deltaElos.get(1),
                            match.getResult().equals("0-1") ? -1 : match.getResult().equals("1/2-1/2") ? -1 : 0,
                            0, 0, 0, 0, 0);
                }
            } catch (Exception neo4jRollbackEx) {
                logger.error("Errore durante il rollback delle statistiche Neo4j", neo4jRollbackEx);
            }
            // rilancio per innescare il rollback su MongoDB
            throw new RuntimeException("Errore aggiornando le statistiche su Neo4j, rollback MongoDB eseguito", e);
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
