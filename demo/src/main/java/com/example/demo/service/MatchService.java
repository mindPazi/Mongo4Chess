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

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionContext;

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

    @Transactional // Questa gestirà il rollback/commit per MongoDB
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
            // Questo scenario potrebbe indicare un errore di sincronizzazione o che i giocatori non sono stati creati correttamente
            throw new Exception("Uno o entrambi i giocatori non esistono in Neo4j. Impossibile procedere.");
        }
        match.setWhiteElo(whiteEloOpt.get());
        match.setBlackElo(blackEloOpt.get());

        // 3) calcolo deltaElo (clamp a zero)
        List<Integer> deltaElos = new ArrayList<>(PlayerService.calculateNewElo(match));
        if (match.getWhiteElo() + deltaElos.get(0) < 0)
            deltaElos.set(0, 0);
        if (match.getBlackElo() + deltaElos.get(1) < 0)
            deltaElos.set(1, 0);

        // Persistenza in MongoDB (Transazione MongoDB) ---
        try {
            persistMatchInMongo(match);
        } catch (Exception e) {
            logger.error("Errore durante la persistenza in MongoDB. Transazione MongoDB annullata.", e);
            throw new RuntimeException("Errore nel salvataggio del match, consulta i log.", e);
        }

        // Aggiornamento Neo4j (Transazione Neo4j) ---
        try {
            updateNeo4jStatsAndRelations(match, deltaElos);
        } catch (Neo4jException e) {
            logger.error("Errore durante l'aggiornamento delle statistiche in Neo4j.", e);
            // se Neo4j fallisce, compensazione MongoDB
            compensateMongoMatchSave(match);
            throw new RuntimeException("Errore nell'aggiornamento di Neo4j. Eseguito rollback su MongoDB. Consulta i log.", e);
        }

        logger.info("Match salvato con successo in MongoDB e Neo4j.");
    }

    @Transactional//("mongoTransactionManager") // Questa transazione garantisce l'atomicità delle operazioni MongoDB
    protected void persistMatchInMongo(Match match) {
        // 4) persistenza in MongoDB
        matchDAO.saveMatch(match); // Salva il documento Match
        logger.debug("Match salvato in matchDAO.");

        // 4.1) aggiunta match ai giocatori in MongoDB
        // Assicurati che PlayerMatch abbia l'ID del match se serve per la compensazione
        PlayerMatch whitePlayerMatch = new PlayerMatch(match.getWhiteElo(), match.getDate());
        PlayerMatch blackPlayerMatch = new PlayerMatch(match.getBlackElo(), match.getDate());

        playerDAO.addMatch(match.getWhite(), whitePlayerMatch);
        logger.debug("Match aggiunto al giocatore White in MongoDB.");
        playerDAO.addMatch(match.getBlack(), blackPlayerMatch);
        logger.debug("Match aggiunto al giocatore Black in MongoDB.");
    }

    @Transactional
    protected void updateNeo4jStatsAndRelations(Match match, List<Integer> deltaElos) {
        // Utilizziamo una singola transazione Neo4j per tutte le operazioni sul grafo
        // 5) relazione "played" in Neo4j
        playerNodeDAO.setPlayedEdge(match.getWhite(), match.getBlack());

        // 6) aggiornamento statistiche Neo4j
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
        logger.debug("Statistiche Neo4j e relazione PLAYED aggiornate.");
    } // La transazione Neo4j viene commessa o rollbacckata automaticamente qui


    // Metodo di compensazione per MongoDB
    @Transactional//("mongoTransactionManager") // Questa transazione garantisce l'atomicità del rollback su MongoDB
    protected void compensateMongoMatchSave(Match match) {
        logger.warn("Avvio compensazione per MongoDB per match ID: {}", match.getId());
        try {
            // Rimuovi il match salvato
            matchDAO.deleteMatch(match); // Assumi che matchDAO abbia un metodo delete
            logger.debug("Match con ID {} rimosso da matchDAO (compensazione).", match.getId());

            // Rimuovi il match dai giocatori
            playerDAO.removeMatch(match.getWhite(), match.getDate(), match.getWhiteElo());
            playerDAO.removeMatch(match.getBlack(), match.getDate(), match.getBlackElo());
            logger.debug("Match con ID {} rimosso dai giocatori in MongoDB (compensazione).", match.getId());

            logger.info("Compensazione MongoDB per match ID {} completata.", match.getId());
        } catch (Exception e) {
            logger.error("ERRORE CRITICO: Fallimento durante la compensazione MongoDB per match ID {}. Richiede intervento manuale!", match.getId(), e);
            throw new RuntimeException("Fallimento irreversibile della compensazione MongoDB.", e);
        }
    }

    // tutte le delete dei match eliminano i match solo dalla collection match, non dai player,
    // perché quelle info servono al player per recuperare le info sull'elo trend.
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
