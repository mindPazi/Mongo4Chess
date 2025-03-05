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
    private String winner;
    // lista dei giocatori iscritti al torneo, con la posizione raggiunta
    private List<TournamentPlayer> players = new ArrayList<>();
    // lista delle partite più importanti del torneo
    private List<TournamentMatch> matches = new ArrayList<>();

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
    public void addPlayer(TournamentPlayer player) {
        this.players.add(player);
    }

    // Metodo per rimuovere un giocatore
    public void removePlayer(String playerUsername) {
        this.players.removeIf(player -> player.getUsername().equals(playerUsername));
    }
}
