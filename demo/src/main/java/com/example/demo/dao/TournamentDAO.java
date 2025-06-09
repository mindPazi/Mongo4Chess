package com.example.demo.dao;

import com.example.demo.model.*;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;
import java.util.List;

@Repository
public class TournamentDAO {
    private final MongoTemplate mongoTemplate;
    @Autowired
    @Qualifier("primaryReadMongoTemplate")
    private MongoTemplate primaryMongoTemplate;

    public TournamentDAO(MongoTemplate mongoTemplate, MongoClient mongoClient) {
        this.mongoTemplate = mongoTemplate;
        MongoDatabase database = mongoClient.getDatabase("chessDB");

        // Useful for the query getTournamentByCreator
        database.getCollection("TournamentCollection").createIndex(new Document("creator", 1));
    }

    // Method to create and save a tournament
    public Tournament createTournament(Tournament tournament) {
        try {
            Tournament savedTournament = mongoTemplate.save(tournament, "TournamentCollection");
            System.out.println("Tournament created successfully: " + savedTournament.getId());
            return savedTournament;
        } catch (Exception e) {
            System.out.println("Error creating tournament: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteTournament(String tournamentId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        mongoTemplate.remove(query, Tournament.class, "TournamentCollection");
        System.out.println("Tournament deleted successfully: " + tournamentId);
    }


    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = mongoTemplate.findAll(Tournament.class, "TournamentCollection");
        System.out.println("All tournaments retrieved successfully");
        return tournaments;
    }

    public List<Tournament> getAvailableTournaments(int elo) {
        Query query = new Query(Criteria.where("isClosed").is(false).and("eloMin").lte(elo).and("eloMax").gte(elo));
        List<Tournament> tournaments = mongoTemplate.find(query, Tournament.class, "TournamentCollection");
        System.out.println("Active tournaments retrieved successfully");
        return tournaments;
    }


    public List<Tournament> getCreatedTournaments(String creator) {
        Query query = new Query(Criteria.where("creator").is(creator));
        return mongoTemplate.find(query, Tournament.class, "TournamentCollection");
    }


    // Adding a player to the tournament
    public void addPlayer(String tournamentId, TournamentPlayer player) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().push("players", player);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Player " + player.getUsername() + " added to the tournament: " + tournamentId);
    }

    // Removing a player from the tournament
    public void removePlayer(String tournamentId, String player) {
        Query query = new Query(Criteria.where("id").is(tournamentId).and("players.username").is(player));
        Update update = new Update().pull("players", new BasicDBObject("username", player));
        UpdateResult result = mongoTemplate.updateFirst(query, update, Tournament.class);
        if (result.getModifiedCount() > 0) {
            System.out.println("Player " + player + " removed form the tournament: " + tournamentId);
        } else {
            System.out.println("Player " + player + " not found or not removed from the tournament: " + tournamentId);
        }
    }


    public Tournament getTournament(String tournamentId) {
        try{
            Tournament tournament = primaryMongoTemplate.findById(tournamentId, Tournament.class, "TournamentCollection");
            if (tournament == null) {
                throw new IllegalArgumentException("Tournament with ID " + tournamentId + " does not exist");
            }
            return tournament;
        } catch (Exception e) {
            System.out.println("Error getting tournament: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void closeTournament(String tournamentId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().set("isClosed", true);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Tournament closed successfully: " + tournamentId);
    }

    public void openTournament(String tournamentId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().set("isClosed", false);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Tournament opened successfully: " + tournamentId);
    }

    public void updatePositions(List<TournamentPlayer> tournamentPlayers, String tournamentId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().set("players", tournamentPlayers);
        // Find the player with position 1 and update the winner field
        tournamentPlayers.stream()
                .filter(player -> player.getPosition() == 1)
                .findFirst()
                .ifPresent(player -> update.set("winner", player.getUsername()));
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Tournament positions updated successfully: " + tournamentId);
    }

    public void addMatchToTournament(String tournamentId, List<TournamentMatch> matches) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().set("matches", matches);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Matches added to tournament successfully: " + tournamentId);
    }

    public List<Tournament> getTournamentsByDate(Date startDate, Date endDate) {
        Query query = new Query(Criteria.where("startDate").gte(startDate).lte(endDate));
        return mongoTemplate.find(query, Tournament.class, "TournamentCollection");
    }

    public void updateCreator(String currentUsername, String newUsername) {
        Query query = new Query(Criteria.where("creator").is(currentUsername));
        Update update = new Update().set("creator", newUsername);
        mongoTemplate.updateMulti(query, update, Tournament.class);
        System.out.println("Creator updated successfully: " + currentUsername + " to " + newUsername);
    }

    public void updateCreatorUsernameInTournaments(String currentUsername, String newUsername) {
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("creator").is(currentUsername)),
                Update.update("creator", newUsername),
                Tournament.class
        );
    }

    public void updateWinnerUsernameInTournaments(String currentUsername, String newUsername) {
        // Update "winner"
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("winner").is(currentUsername)),
                Update.update("winner", newUsername),
                Tournament.class
        );
    }

        public void updatePlayerUsernameInTournaments(String currentUsername, String newUsername){
            // Update array players[].username using arrayFilters
            Update update = new Update()
                    .set("players.$[elem].username", newUsername)
                    .filterArray(Criteria.where("elem.username").is(currentUsername));

            mongoTemplate.updateMulti(
                    Query.query(Criteria.where("players.username").is(currentUsername)),
                    update,
                    Tournament.class
            );
        }

}
