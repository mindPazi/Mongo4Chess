package com.example.demo.dao;

import com.example.demo.model.Match;
import com.example.demo.model.Player;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PlayerDAO {
    private final MongoCollection<Document> playerCollection;

    public PlayerDAO(MongoClient mongoclient) {
        MongoDatabase mongodatabase = mongoclient.getDatabase("chessDB");
        this.playerCollection = mongodatabase.getCollection("PlayerCollection");
    }

    public void createPlayer(String username, String password) {
        playerCollection.insertOne(new Document("username", username)
                .append("password", password));
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

    public Player getPlayer(String player) {
        return convertDocumentToPlayer(
                playerCollection.find(new Document("username", player)).into(new ArrayList<>()).get(0));
    }

    private Player convertDocumentToPlayer(Document doc) {
        Player player = new Player();
        player.setUsername(doc.getString("username"));
        player.setPassword(doc.getString("password"));
        player.setIsBanned(doc.getBoolean("banned", false));
        player.setMatches((List<Match>) doc.get("matches"));
        return player;
    }

    private Document convertPlayerToDocument(Player player) {
        return new Document("username", player.getUsername())
                .append("password", player.getPassword());
    }


}
