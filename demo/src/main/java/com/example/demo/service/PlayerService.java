package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerDAO;
import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.dao.TournamentDAO;
import com.example.demo.model.*;
import com.mongodb.MongoException;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.exceptions.Neo4jException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private final PlayerDAO playerDAO;
    private final PlayerNodeDAO playerNodeDAO;
    private final MatchDAO matchDAO;
    private final TournamentDAO tournamentDAO;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final static int EloConstant = 30;


    public PlayerService(PlayerDAO playerDAO, PlayerNodeDAO playerNodeDAO, MatchDAO matchDAO,
            TournamentDAO tournamentDAO) {
        this.playerDAO = playerDAO;
        this.playerNodeDAO = playerNodeDAO;
        this.matchDAO = matchDAO;
        this.tournamentDAO = tournamentDAO;
    }

    public void banPlayer(String playerUsername) {
        try {
            playerDAO.banPlayer(playerUsername);
        } catch (RuntimeException e) {
            logger.error("Error banning player: {}", playerUsername, e);
            throw new RuntimeException("Errore banning: " + playerUsername);
        }
    }

    public void unBanPlayer(String playerUsername) {
        try {
            playerDAO.unBanPlayer(playerUsername);
        } catch (Exception e) {
            logger.error("Error unbanning player\n: {}", playerUsername, e);
            throw new RuntimeException("Error unbanning: " + playerUsername);
        }
    }

    public void deletePlayer(String playerUsername) {
        // backup in caso di rollback manuale su Neo4j
        Player mongoPlayerBackup = playerDAO.getPlayer(playerUsername);

        // se playerDAO.deletePlayer lancia RuntimeException, Spring fa rollback
        // automatico
        playerDAO.deletePlayer(playerUsername);

        try {
            playerNodeDAO.deletePlayer(playerUsername);
        } catch (Neo4jException neo4jEx) {
            // qui vogliamo comunque cancellare la rollback su Mongo
            if (mongoPlayerBackup != null) {
                playerDAO.rollbackPlayer(mongoPlayerBackup);
            }
            logger.error("Error deleting in Neo4j, rollback in MongoDB executed. {}", neo4jEx.getMessage(), neo4jEx);
            throw new RuntimeException("Error deleting the player");
        }
    }

    public void updatePlayerPassword(String oldPassword, String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Player player = playerDAO.getPlayer(currentUsername);

            if (!encoder.matches(oldPassword, player.getPassword())) {
                throw new IllegalArgumentException("The old paassword is incorrect.");
            }

            String hashedNewPassword = encoder.encode(newPassword);

            playerDAO.updatePlayerPassword(currentUsername, hashedNewPassword);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating the password: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating the password");
        }
    }

    @Transactional("mongoTransactionManager")  //for mongo
    public void updatePlayerUsername(String newUsername) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        try {
            playerNodeDAO.updatePlayerUsername(currentUsername, newUsername);
            playerDAO.updatePlayerUsername(currentUsername, newUsername);
            matchDAO.updatePlayerUsernameInMatches(currentUsername, newUsername);
            tournamentDAO.updatePlayerUsernameInTournaments(currentUsername, newUsername);
            tournamentDAO.updateCreatorUsernameInTournaments(currentUsername, newUsername);
            tournamentDAO.updateWinnerUsernameInTournaments(currentUsername, newUsername);
        } catch (Neo4jException neo4jException) {
            // so if both neo4j and mongo throw exception, I exit immediately without trying to rollback on neo4j
            logger.error("Error updating the username in Neo4j from " + currentUsername + " to " + newUsername, neo4jException);
            throw new RuntimeException("Error updating the username from " + currentUsername + " to " + newUsername);
        } catch (MongoException mongoException) {
            // Automatic rollback on neo4j on MongoDB failure
            playerNodeDAO.updatePlayerUsername(newUsername, currentUsername);
            logger.error("Error updating the username from " + currentUsername + " to"  + newUsername + " in MongoDB, rollback in Neo4j executed!", mongoException);
            throw new RuntimeException("Error updating the username from " + currentUsername + " to " + newUsername);
        } catch (Exception e) {
            logger.error("Error updating the username in Neo4j from " + currentUsername + " to " + newUsername, e);
            throw new RuntimeException(
                    "Error updating the username from " + currentUsername + " to " + newUsername);
        }
    }

    // I see the satistics of the player
    public PlayerNode getStats(String playerId) {
        return playerNodeDAO.getStats(playerId);
    }

    public List<PlayerMatch> getEloTrend(String playerId) {
        try {
            return playerDAO.getEloTrend(playerId);
        } catch (Exception e) {
            logger.error("Error getting the elo trend for {}", playerId, e);
            throw new RuntimeException("Error getting the elo trend for " + playerId);
        }
    }

    public void addFriend(String friendId) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();

            if(playerNodeDAO.getPlayerById(friendId)==null)
                throw new RuntimeException("Player not found " + friendId);

            playerNodeDAO.addFriend(playerId, friendId);
        } catch (Exception e) {
            logger.error("Error adding friend: ", friendId, e.getMessage(), e);
            throw new RuntimeException("Error adding friend " + friendId);
        }
    }

    public List<PlayerNode> getFriends() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();
            return playerNodeDAO.getFriends(playerId);
        } catch (Exception e) {
            logger.error("Error getting firend list.", e.getMessage(), e);
            throw new RuntimeException("Error getting firend list.");
        }
    }

    public List<String> matchmaking(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();
            return playerNodeDAO.matchmaking(playerId);
        }catch(Exception e){
            logger.error("Errore durante il matchmaking", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero del matchmaking");
        }
    }

    public List<String> pathToPlayed(){
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();

            List<String> flatPaths = playerNodeDAO.pathToPlayed(playerId);

            List<String> paths = new ArrayList<>();
            List<String> currentPath = new ArrayList<>();

            for (String node : flatPaths) {
                if (node.equals(playerId)) {
                    if (!currentPath.isEmpty()) {
                        paths.add(String.join(" -> ", currentPath));
                        currentPath.clear();
                    }
                }
                currentPath.add(node);
            }

            if (!currentPath.isEmpty()) {
                paths.add(String.join(" -> ", currentPath));
            }

            return paths;

        }catch(Exception e){
            logger.error("Errore durante il recupero del percorso di amici", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero del percorso di amici");
        }
    }

    public List<String> pathBetween2Player(String friendId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();
            return playerNodeDAO.pathBetween2Player(playerId,friendId);
        }catch(Exception e){
            logger.error("Errore durante il recupero del percorso di amici", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero del percorso di amici");
        }
    }

    public void removeFriend(String friendId) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();

            int removed = playerNodeDAO.removeFriend(playerId, friendId);

            if (removed == 0) {
                throw new IllegalArgumentException("Friend not found.");
            }
        } catch (Exception e) {
            logger.error("Error removing friend: ", friendId, e.getMessage(), e);
            throw new RuntimeException("Error removing friend: " + friendId);
        }
    }

    //@Transactional is not needed, on mongo there is the insertion of an element on only one collection
    public String createPlayer(String username, String password, int elo) {
        if (playerDAO.getPlayer(username) == null) {
            playerDAO.createPlayer(username, password);
            try {
                playerNodeDAO.createPlayer(username, elo);
                return "Player created with success";
            } catch (Neo4jException neo4jEx) {
                // rollback esplicito su Mongo in caso di fallimento Neo4j
                playerDAO.deletePlayer(username);
                logger.error("Error in Neo4j, rollback in MongoDB executed.", neo4jEx);
                throw new RuntimeException("Error during the registration.");
            }
        } else {
            return "Username already taken.";
        }
    }

    // calculated with this formula: https://it.wikipedia.org/wiki/Elo_(scacchi)
    public static List<Integer> calculateNewElo(Match match) {
        int newWhiteElo, newBlackElo;
        double whiteExpectedScore = expectedScore(match.getWhiteElo(), match.getBlackElo());
        double blackExpectedScore = expectedScore(match.getBlackElo(), match.getWhiteElo());
        double whiteScore = match.getResult().equals("1-0") ? 1 : match.getResult().equals("0-1") ? 0 : 0.5;
        double blackScore = 1 - whiteScore;

        newWhiteElo = (int) (match.getWhiteElo() + EloConstant * (whiteScore - whiteExpectedScore));
        newBlackElo = (int) (match.getBlackElo() + EloConstant * (blackScore - blackExpectedScore));

        return List.of(newWhiteElo, newBlackElo);
    }

    private static double expectedScore(int myElo, int opponentElo) {
        return 1 / (1 + Math.pow(10, (double) (opponentElo - myElo) / 400));
    }

    public List<PlayerTournament> getMyTournaments(String playerId) {
        return playerDAO.getMyTournaments(playerId);
    }
}
