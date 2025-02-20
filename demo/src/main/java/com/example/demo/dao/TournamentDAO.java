package com.example.demo.dao;

import com.example.demo.model.Tournament;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Player;
import com.example.demo.model.Match;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Repository
public class TournamentDAO {
    private final MongoTemplate mongoTemplate;

    public TournamentDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveTournament(Tournament tournament) {
        mongoTemplate.save(tournament, "TournamentCollection");
        System.out.println("Tournament saved successfully: " + tournament.toString());
    }

    public void deleteTournament(String tournamentId) {
        mongoTemplate.remove(mongoTemplate.findById(tournamentId, Tournament.class));
        System.out.println("Tournament deleted successfully: " + tournamentId);
    }

    public void updateTournament(Tournament tournament) {
        mongoTemplate.save(tournament, "TournamentCollection"); // MongoDB aggiorna se l'ID esiste
        System.out.println("Tournament updated successfully: " + tournament);
    }

    public void addWinner(String tournamentId, Player winner) {
        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
        if (tournament != null) {
            tournament.setWinner(winner);
            mongoTemplate.save(tournament);
            System.out.println("Winner added successfully: " + winner + " to tournament: " + tournamentId);
        } else {
            System.out.println("Tournament not found: " + tournamentId);
        }
    }

    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = mongoTemplate.findAll(Tournament.class, "TournamentCollection");
        System.out.println("All tournaments retrieved successfully");
        return tournaments;
    }

    public List<Tournament> getActiveTournaments() {
        List<Tournament> tournaments = mongoTemplate.find(new Query(), Tournament.class, "TournamentCollection");
        System.out.println("Active tournaments retrieved successfully");
        return tournaments;
    }

    public void addMostImportantMatches(List<Match> matches, String tournamentId) {
        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
        if (tournament != null) {
            tournament.setMatches(matches);
            mongoTemplate.save(tournament);
            System.out.println("Most important matches added successfully to tournament: " + tournamentId);
        } else {
            System.out.println("Tournament not found: " + tournamentId);
        }
    }

    public void joinTournament(String tournamentId, String playerUsername) {
        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
        if (tournament != null) {
            tournament.addPlayer(playerUsername);
            mongoTemplate.save(tournament);
            System.out.println(
                    "Player joined tournament successfully: " + playerUsername + " to tournament: " + tournamentId);
        } else {
            System.out.println("Tournament not found: " + tournamentId);
        }
    }

    public void deletePlayerFromTournament(String tournamentId, String playerUsername) {
        Tournament tournament = mongoTemplate.findById(tournamentId, Tournament.class);
        if (tournament != null) {
            tournament.removePlayer(playerUsername);
            mongoTemplate.save(tournament);
            System.out.println("Player removed from tournament successfully: " + playerUsername + " from tournament: "
                    + tournamentId);
        } else {
            System.out.println("Tournament not found: " + tournamentId);
        }
    }

    public String getCreatedTournaments(String creator) {
        List<Tournament> tournaments = mongoTemplate.find(new Query(), Tournament.class, "TournamentCollection");
        StringBuilder createdTournaments = new StringBuilder();
        for (Tournament tournament : tournaments) {
            if (tournament.getCreator().equals(creator)) {
                createdTournaments.append(tournament.toString());
            }
        }
        return createdTournaments.toString();
    }

    public void addMatch(String tournamentId, Match match) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().push("matches", match);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Match aggiunto con successo al torneo: " + tournamentId);
    }

    // Aggiunge un giocatore al torneo
    public void addPlayer(String tournamentId, String playerId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().push("players", playerId);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Giocatore " + playerId + " aggiunto al torneo: " + tournamentId);
    }

    // Rimuove un giocatore dal torneo
    public void removePlayer(String tournamentId, String playerId) {
        Query query = new Query(Criteria.where("id").is(tournamentId));
        Update update = new Update().pull("players", playerId);
        mongoTemplate.updateFirst(query, update, Tournament.class);
        System.out.println("Giocatore " + playerId + " rimosso dal torneo: " + tournamentId);
    }
}
