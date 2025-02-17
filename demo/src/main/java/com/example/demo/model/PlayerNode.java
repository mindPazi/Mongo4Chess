package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Node("PlayerNode")
public class PlayerNode {

        @Id
        @GeneratedValue // Neo4j genererà automaticamente l'ID
        private Long id;

        private String username;
        private int elo;
        private int blackWins;
        private int whiteWins;
        private int whiteDraws;
        private int blackDraws;
        private int whiteLosses;
        private int blackLosses;
        private boolean isBanned;

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
                this.isBanned = false;
        }

        // Getter e Setter
        public Long getId() {
                return id;
        }

        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public int getElo() {
                return elo;
        }

        public void setElo(int elo) {
                this.elo = elo;
        }

        public int getBlackWins() {
                return blackWins;
        }

        public void setBlackWins(int blackWins) {
                this.blackWins = blackWins;
        }

        public int getWhiteWins() {
                return whiteWins;
        }

        public void setWhiteWins(int whiteWins) {
                this.whiteWins = whiteWins;
        }

        public int getWhiteDraws() {
                return whiteDraws;
        }

        public void setWhiteDraws(int whiteDraws) {
                this.whiteDraws = whiteDraws;
        }

        public int getBlackDraws() {
                return blackDraws;
        }

        public void setBlackDraws(int blackDraws) {
                this.blackDraws = blackDraws;
        }

        public int getWhiteLosses() {
                return whiteLosses;
        }

        public void setWhiteLosses(int whiteLosses) {
                this.whiteLosses = whiteLosses;
        }

        public int getBlackLosses() {
                return blackLosses;
        }

        public void setBlackLosses(int blackLosses) {
                this.blackLosses = blackLosses;
        }

        public boolean isBanned() {
                return isBanned;
        }

        public void setBanned(boolean banned) {
                isBanned = banned;
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
                                ", isBanned=" + isBanned +
                                '}';
        }
}
