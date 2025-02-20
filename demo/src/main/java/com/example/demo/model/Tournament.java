package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Document(collection = "TournamentCollection")
public class Tournament {
    @Id
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
    private List<Map<String, Integer>> players = new ArrayList<>();
    // lista delle partite più importanti del torneo
    private List<Match> matches = new ArrayList<>();

    public Tournament(String name, int eloMin, int eloMax, int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.name = name;
        this.eloMin = eloMin;
        this.eloMax = eloMax;
        this.startDate = new Date();
    }

    public Tournament() {
    }

    // Metodo per aggiungere un giocatore
    public void addPlayer(String playerUsername) {
        Map<String, Integer> playerEntry = new HashMap<>();
        playerEntry.put(playerUsername, 0); // 0 come posizione iniziale
        this.players.add(playerEntry);
    }

    // Metodo per rimuovere un giocatore
    public void removePlayer(String playerUsername) {
        this.players.removeIf(playerMap -> playerMap.containsKey(playerUsername));
    }
}
