package com.example.demo.dao;

import com.example.demo.model.Tournament;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Player;

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
}
