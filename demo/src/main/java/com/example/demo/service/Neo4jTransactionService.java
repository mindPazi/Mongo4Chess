package com.example.demo.service;

import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.model.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class Neo4jTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jTransactionService.class);

    private final PlayerNodeDAO playerNodeDAO;

    @Autowired
    public Neo4jTransactionService(PlayerNodeDAO playerNodeDAO) {
        this.playerNodeDAO = playerNodeDAO;
    }

    @Transactional("neo4jTransactionManager")
    public void updateNeo4jStatsAndRelations(Match match, List<Integer> deltaElos) {
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
    }
}
