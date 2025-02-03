package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("Player")
public class PlayerNode {

    @Id
    private String id;
    private String username;
    private int elo;
    private int blackWins;
    private int whiteWins;
    private int whiteDraws;
    private int blackDraws;
    private int whiteLosses;
    private int blackLosses;

    public PlayerNode(String username, int elo, int blackWins, int whiteWins, int whiteDraws, int blackDraws,
            int whiteLosses, int blackLosses) {
        this.username = username;
        this.elo = elo;
        this.blackWins = blackWins;
        this.whiteWins = whiteWins;
        this.whiteDraws = whiteDraws;
        this.blackDraws = blackDraws;
        this.whiteLosses = whiteLosses;
        this.blackLosses = blackLosses;
    }

    public PlayerNode(String username, int elo) {
        this.username = username;
        this.elo = elo;
    }
}