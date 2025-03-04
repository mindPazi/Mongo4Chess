package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerNodeDAO;
import org.bson.Document;

import com.example.demo.model.Match;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service

public class MatchService {
    private final MatchDAO matchDAO;
    private final PlayerNodeDAO playerNodeDAO;

    public MatchService(MatchDAO matchDAO, PlayerNodeDAO playerNodeDAO) {
        this.matchDAO = matchDAO;
        this.playerNodeDAO = playerNodeDAO;
    }

    //todo: gestire la consistenza
    public void saveMatch(Match match) throws Exception {

        Optional<Integer> whiteEloOpt = playerNodeDAO.getElo(match.getWhite());
        Optional<Integer> blackEloOpt = playerNodeDAO.getElo(match.getBlack());

        if (whiteEloOpt.isEmpty() || blackEloOpt.isEmpty()) {
            throw new Exception("Uno o entrambi i giocatori non esistono");
        }

        if(match.getWhite().equals(match.getBlack())){
            throw new Exception("Un giocatore non può giocare contro se stesso");
        }

        match.setWhiteElo(whiteEloOpt.get());
        match.setBlackElo(blackEloOpt.get());

        //il match viene salvato dopo aver calcolato l'elo prima del match
        matchDAO.saveMatch(match);

        //calculate new Elo
        List<Integer> deltaElos = new java.util.ArrayList<>(PlayerService.calculateNewElo(match));

        // se l'aggiornamento dell'elo è negativo, lo setta a 0
        if (match.getWhiteElo() + deltaElos.get(0) < 0)
            deltaElos.set(0, 0);
        if (match.getBlackElo() + deltaElos.get(1) < 0)
            deltaElos.set(1, 0);

        //aggiorna le statistiche del giocatore sul nodo
        if (match.getResult().equals("1-0")) {
            playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 1, 0, 0, 0, 0, 0);
            playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 0, 0, 1);
        } else if (match.getResult().equals("0-1")) {
            playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 0, 0, 1, 0);
            playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 1, 0, 0, 0, 0);
        } else {
            playerNodeDAO.updatePlayerStats(match.getWhite(), deltaElos.get(0), 0, 0, 1, 0, 0, 0);
            playerNodeDAO.updatePlayerStats(match.getBlack(), deltaElos.get(1), 0, 0, 0, 1, 0, 0);
        }

    }

//    public void createMatch(Match match) {
//        matchDAO.saveMatch(match);
//    }

    public void deleteAllMatches() {
        matchDAO.deleteAllMatches();
    }

    public void deleteAllMatchesByPlayer(String player) {
        matchDAO.deleteAllMatchesByPlayer(player);
    }

    public int getNumOfWinsAndDrawsPerElo(int elomin, int elomax) {
        return matchDAO.getNumOfWinsAndDrawsPerElo(elomin, elomax);
    }

    public List<Document> getMatches() {
        return matchDAO.getMatches();
    }

    public List<Document> getMatchesByPlayer(String player) {
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
}
