package com.example.demo.service;
import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerDAO;
import com.example.demo.model.Match;
import com.example.demo.model.PlayerMatch;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MongoTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(MongoTransactionService.class);

    private final MatchDAO matchDAO;
    private final PlayerDAO playerDAO;
    private Match savedMatch;

    @Autowired
    public MongoTransactionService(MatchDAO matchDAO, PlayerDAO playerDAO) {
        this.matchDAO = matchDAO;
        this.playerDAO = playerDAO;
    }

    @Transactional("mongoTransactionManager")
    public void persistMatchInMongo(Match match) {
        savedMatch = matchDAO.saveMatch(match); // Save the Match document
        logger.debug("Match saved in matchDAO.");

        // Convert String to ObjectId
        ObjectId matchObjectId = new ObjectId(savedMatch.getId());

        // Adding Matches to Players in MongoDB
        PlayerMatch whitePlayerMatch = new PlayerMatch(match.getWhiteElo(), match.getDate(), matchObjectId);
        PlayerMatch blackPlayerMatch = new PlayerMatch(match.getBlackElo(), match.getDate(), matchObjectId);

        playerDAO.addMatch(match.getWhite(), whitePlayerMatch);
        logger.debug("Match added to white player in MongoDB.");
        playerDAO.addMatch(match.getBlack(), blackPlayerMatch);
        logger.debug("Match added to black player in MongoDB.");
    }

    // Compensation method for MongoDB : if Neo4j transaction
    // fails, the rollback on MongoDB is performed.
    @Transactional("mongoTransactionManager")
    public void compensateMongoMatchSave(Match match) {
        logger.warn("Start compensation for MongoDB for ID match: {}", savedMatch.getId());
        try {
            // Saved match removed
            matchDAO.deleteMatch(savedMatch.getId());
            logger.debug("Match with ID {} removed from matchDAO (compensation).", savedMatch.getId());

            ObjectId matchObjectId = new ObjectId(savedMatch.getId());

            // Match removed from player's document
            playerDAO.removeMatch(match.getWhite(), matchObjectId);
            playerDAO.removeMatch(match.getBlack(), matchObjectId);
            logger.debug("Match with ID {} removed from players in MongoDB (compensation).", savedMatch.getId());
            logger.info("MongoDB compensation for match ID {} completed.", savedMatch.getId());
        } catch (Exception e) {
            logger.error("FATAL ERROR: Failure while clearing MongoDB for match ID {}. Requires manual intervention!", match.getId(), e);
            throw new RuntimeException("Error during MongoDB compensation for match: " + savedMatch.getId(), e);
        }
    }
}