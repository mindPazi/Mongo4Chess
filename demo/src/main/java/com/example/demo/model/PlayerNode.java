package com.example.demo.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.UUID;

@Data
@Node("PlayerNode")
public class PlayerNode {

    @Id
    @GeneratedValue // Neo4j genererà automaticamente l'ID
    private UUID id;

    @Property("username")
    private String username;
    @Property("elo")
    private int elo;
    @Property("blackWins")
    private int blackWins;
    @Property("whiteWins")
    private int whiteWins;
    @Property("whiteDraws")
    private int whiteDraws;
    @Property("blackDraws")
    private int blackDraws;
    @Property("whiteLosses")
    private int whiteLosses;
    @Property("blackLosses")
    private int blackLosses;
    //is banned è già salvato in mongo
//    @Property("isBanned")
//    private boolean isBanned;

    // Costruttore senza parametri (necessario per Spring Data Neo4j)
    public PlayerNode() {
    }

    // Costruttore con username ed elo
    public PlayerNode(String username, int elo) {
        this.username = username;
        this.elo = elo;
        this.blackWins = 0;
        this.whiteWins = 0;
        this.whiteDraws = 0;
        this.blackDraws = 0;
        this.whiteLosses = 0;
        this.blackLosses = 0;
        //this.isBanned = false;
    }

    @Override
    public String toString() {
        return "PlayerNode{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", elo=" + elo +
               ", blackWins=" + blackWins +
               ", whiteWins=" + whiteWins +
               ", whiteDraws=" + whiteDraws +
               ", blackDraws=" + blackDraws +
               ", whiteLosses=" + whiteLosses +
               ", blackLosses=" + blackLosses +
//                                ", isBanned=" + isBanned +
               '}';
    }
}
