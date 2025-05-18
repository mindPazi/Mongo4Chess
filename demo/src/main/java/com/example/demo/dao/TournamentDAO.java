package com.example.demo.dao;

import com.example.demo.model.*;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class TournamentDAO {
    private final MongoTemplate mongoTemplate;

    public TournamentDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Metodo per creare e salvare un torneo
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

    public void updateTournament(Tournament tournament) {
        mongoTemplate.save(tournament, "TournamentCollection"); // MongoDB aggiorna se l'ID esiste
        System.out.println("Tournament updated successfully: " + tournament);
    }


    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = mongoTemplate.findAll(Tournament.class, "TournamentCollection");
        System.out.println("All tournaments retrieved successfully");
        return tournaments;
    }

    public List<Tournament> getActiveTournaments(int elo) {
        Query query = new Query(Criteria.where("isClosed").is(false).and("eloMin").lte(elo).and("eloMax").gte(elo));
        List<Tournament> tournaments = mongoTemplate.find(query, Tournament.class, "TournamentCollection");
        System.out.println("Active tournaments retrieved successfully");
        return tournaments;
    }

//    public void addMostImportantMatches(List<Match> matches, String tournamentId) {
//        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
//        if (tournament != null) {
//            tournament.setMatches(matches);
//            mongoTemplate.save(tournament);
//            System.out.println("Most important matches added successfully to tournament: " + tournamentId);
//        } else {
//            System.out.println("Tournament not found: " + tournamentId);
//        }
//    }

    public void joinTournament(String tournamentId, TournamentPlayer player) {
        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
        if (tournament != null) {
            tournament.addPlayer(player);
            mongoTemplate.save(tournament);
            System.out.println(
                    "Player joined tournament successfully: " + player.getUsername() + " to tournament: " + tournamentId);
        } else {
            System.out.println("Tournament not found: " + tournamentId);
        }
    }

//    public void deletePlayerFromTournament(String tournamentId, String playerUsername) {
//        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
//        if (tournament != null) {
//            tournament.removePlayer(playerUsername);
//            mongoTemplate.save(tournament);
//            System.out.println("Player removed from tournament successfully: " + playerUsername + " from tournament: "
//                    + tournamentId);
//        } else {
//            System.out.println("Tournament not found: " + tournamentId);
//        }
//    }

    public List<Tournament> getCreatedTournaments(String creator) {
        Query query = new Query(Criteria.where("creator").is(creator));
        return mongoTemplate.find(query, Tournament.class, "TournamentCollection");

//        StringBuilder createdTournaments = new StringBuilder();
//        for (Tournament tournament : tournaments) {
//            if (tournament.getCreator().equals(creator)) {
//                createdTournaments.append(tournament.toString());
//            }
//        }
//        return createdTournaments.toString();
    }

    public void addMatch(String tournamentId, Match match) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().push("matches", match);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Match aggiunto con successo al torneo: " + tournamentId);
    }

    // Aggiunge un giocatore al torneo
    public void addPlayer(String tournamentId, TournamentPlayer player) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().push("players", player);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Giocatore " + player.getUsername() + " aggiunto al torneo: " + tournamentId);
    }

    // Rimuove un giocatore dal torneo
    public void removePlayer(String tournamentId, String player) {
        Query query = new Query(Criteria.where("id").is(tournamentId).and("players.username").is(player));
        Update update = new Update().pull("players", new BasicDBObject("username", player));
        UpdateResult result = mongoTemplate.updateFirst(query, update, Tournament.class);
        if (result.getModifiedCount() > 0) {
            System.out.println("Giocatore " + player + " rimosso dal torneo: " + tournamentId);
        } else {
            System.out.println("Giocatore " + player + " non trovato o non rimosso dal torneo: " + tournamentId);
        }
    }


    public Tournament getTournament(String tournamentId) {
        try{
            return mongoTemplate.findById(tournamentId, Tournament.class, "TournamentCollection");
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
        Update update = new Update().set("closed", false);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Tournament opened successfully: " + tournamentId);
    }

    public void updatePositions(List<TournamentPlayer> tournamentPlayers, String tournamentId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().set("players", tournamentPlayers);
        // Trova il player con posizione 1 e aggiorna il campo winner
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

    public void updatePlayerUsernameInTournaments(String currentUsername, String newUsername) {
        Query query = new Query(Criteria.where("players.username").is(currentUsername).orOperator(Criteria.where("creator").is(currentUsername)));
        Update update = new Update().set("players.$.username", newUsername).set("creator", newUsername);
        mongoTemplate.updateMulti(query, update, Tournament.class);
        System.out.println("Player username and creator updated successfully: " + currentUsername + " to " + newUsername);
    }
}
