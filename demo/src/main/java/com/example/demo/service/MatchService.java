package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import org.bson.Document;

import com.example.demo.model.Match;

import java.util.List;

import org.springframework.stereotype.Service;

@Service

public class MatchService {
    private final MatchDAO matchDAO;

    public MatchService(MatchDAO matchDAO) {
        this.matchDAO = matchDAO;
    }

    //todo: aggiungere il match anche al player
    public void saveMatch(Match match) {
        matchDAO.saveMatch(match);
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
}
