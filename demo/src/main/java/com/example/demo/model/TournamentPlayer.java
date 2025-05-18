package com.example.demo.model;

import lombok.Data;

@Data
public class TournamentPlayer {
    String username;
    int position;

    public TournamentPlayer(String username, int position) {
        this.username = username;
        this.position = position;
    }

    public TournamentPlayer() {
    }
}
