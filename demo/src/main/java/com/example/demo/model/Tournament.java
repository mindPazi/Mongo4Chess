package com.example.demo.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.example.demo.model.Tournament;

@Data
@Document(collection = "TournamentCollection")

public class Tournament {
    private String id;
    private String creator;
    private String description;
    private int maxPlayers;
    private String name;
    private Date startDate;
    private Date endDate;
    private Boolean isClosed;
    private int eloMin;
    private int eloMax;
    private Player winner;
    // lista dei giocatori iscritti al torneo, con la posizione raggiunta
    private List<Map<String, Integer>> players;
    // lista delle partite più importanti del torneo
    private List<Match> matches;

    // todo: gestire il fatto che il numero di players deve essere una potenza del
    // 2, fare un check
    public Tournament(String name, int eloMin, int eloMax, int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.eloMin = eloMin;
        this.eloMax = eloMax;
        this.startDate = new Date();
    }

    public Tournament() {
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public void setPlayers(List<Map<String, Integer>> players) {
        this.players = players;
    }

    public void addMatch(Match match) {
        // add match to tournament in mongodb
    }

    public void addPlayer(String player) {
        // add player to tournament in mongodb
    }

    public void removePlayer(String player) {
        // remove player from tournament in mongodb
    }

}
