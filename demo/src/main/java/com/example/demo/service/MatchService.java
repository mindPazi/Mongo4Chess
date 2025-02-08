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

    public void saveMatch(Match match) {
        matchDAO.saveMatch(match);
    }

    public void deleteAllMatches() {
        matchDAO.deleteAllMatches();
    }

    public void deleteAllMatchesByPlayer(String player) {
        matchDAO.deleteAllMatchesByPlayer(player);
    }

    public List<Document> getMostPlayedOpenings(int elomin, int elomax) {
        return matchDAO.getMostPlayedOpenings(elomin, elomax);
    }

    public int getNumOfWinsAndDrawsPerElo(int elomin, int elomax) {
        return matchDAO.getNumOfWinsAndDrawsPerElo(elomin, elomax);
    }

}
