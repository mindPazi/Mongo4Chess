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
    // list of players registered for the tournament, with the position achieved
    private List<TournamentPlayer> players = new ArrayList<>();
    // list of the most important matches of the tournament
    private List<TournamentMatch> matches = new ArrayList<>();

    public Tournament() {
    }
}
