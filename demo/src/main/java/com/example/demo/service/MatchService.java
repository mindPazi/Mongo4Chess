package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerDAO;

import com.example.demo.model.Match;
import org.springframework.stereotype.Service;

@Service

public class MatchService {
    private final MatchDAO matchDAO;
    private final PlayerDAO playerDAO;

    public MatchService(MatchDAO matchDAO, PlayerDAO playerDAO) {
        this.matchDAO = matchDAO;
        this.playerDAO = playerDAO;
    }

    public void saveMatch(Match match) {
        matchDAO.saveMatch(match);
    }

    public void deleteAllMatches() {
        matchDAO.deleteAllMatches();
    }

}
