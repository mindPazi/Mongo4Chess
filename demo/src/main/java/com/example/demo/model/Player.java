package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "PlayerCollection")

public class Player {
    @Id
    private String id;
    private String username;
    private String password;
    private List<PlayerMatch> matches;
    private List<PlayerTournament> tournaments;
    private Boolean isBanned;

    // constructor for new players
    public Player(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Player() {
    }

}
