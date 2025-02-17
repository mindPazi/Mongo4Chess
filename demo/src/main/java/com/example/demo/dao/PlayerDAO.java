package com.example.demo.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class PlayerDAO {
    private final MongoCollection<Document> playerCollection;

    public PlayerDAO(MongoClient mongoclient) {
        MongoDatabase mongodatabase = mongoclient.getDatabase("chessDB");
        this.playerCollection = mongodatabase.getCollection("PlayerCollection");
    }

    public void createPlayer(String username, String password, int elo) {
        Document player = new Document("username", username)
                .append("password", password)
                .append("elo", elo)
                .append("blackWins", 0)
                .append("whiteWins", 0)
                .append("whiteDraws", 0)
                .append("blackDraws", 0)
                .append("whiteLosses", 0)
                .append("blackLosses", 0)
                .append("isBanned", false);
        playerCollection.insertOne(player);
    }

    public void banPlayer(String username) {
        playerCollection.updateOne(new Document("username", username),
                new Document("$set", new Document("isBanned", true)));
    }

    public void unBanPlayer(String username) {
        playerCollection.updateOne(new Document("username", username),
                new Document("$set", new Document("isBanned", false)));
    }

    public void deletePlayer(String username) {
        playerCollection.deleteOne(new Document("username", username));
    }

    public void updatePlayerPassword(String oldUsername, String newPassword) {
        playerCollection.updateOne(new Document("username", oldUsername),
                new Document("$set", new Document("password", newPassword)));
    }

    public void updatePlayerUsername(String oldUsername, String newUsername) {
        playerCollection.updateOne(new Document("username", oldUsername),
                new Document("$set", new Document("username", newUsername)));
    }

    public void getStats() {
        // get stats
    }

    public String getEloTrend(String username) {
        // get elo trend
        return "Elo trend";
    }

}
