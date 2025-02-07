package com.example.demo.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class PlayerDAO {
    private final MongoCollection<Document> playerCollection;

    public PlayerDAO(MongoClient mongoclient) {
        MongoDatabase mongodatabase = mongoclient.getDatabase("chessDB");
        this.playerCollection = mongodatabase.getCollection("PlayerCollection");
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
}
