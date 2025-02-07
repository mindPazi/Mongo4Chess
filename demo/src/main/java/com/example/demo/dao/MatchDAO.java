package com.example.demo.dao;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import com.example.demo.model.Match;

@Repository
public class MatchDAO {
    private final MongoCollection<Document> matchCollection;

    public MatchDAO(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.matchCollection = database.getCollection("MatchCollection");
    }

    public void saveMatch(Match match) {
        Document matchDocument = Document.parse(match.toJson());
        matchCollection.insertOne(matchDocument);
    }

    public void deleteAllMatches() {
        matchCollection.deleteMany(new Document());
    }

    public void deleteMatchByDate(String start_date, String end_date, String player) {
        Document query = new Document();
        query.append("date", new Document("$gte", start_date).append("$lte", end_date));
        query.append("$or", new Document("white", player).append("black", player));
        matchCollection.deleteMany(query);
    }

}
