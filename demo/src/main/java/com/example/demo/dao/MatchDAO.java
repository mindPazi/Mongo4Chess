package com.example.demo.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Match;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        query.append("$or", Arrays.asList(
                new Document("white", player),
                new Document("black", player)));
        matchCollection.deleteMany(query);
    }

    public void deleteAllMatchesByPlayer(String player) {
        Document query = new Document("$or", Arrays.asList(
                new Document("white", player),
                new Document("black", player)));
        matchCollection.deleteMany(query);
    }

    // ✅ Metodo getMostPlayedOpenings()
    public List<Document> getMostPlayedOpenings(int elomin, int elomax) {
        AggregateIterable<Document> results = matchCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("$and", Arrays.asList(
                        new Document("whiteElo", new Document("$gte", elomin).append("$lte", elomax)),
                        new Document("blackElo", new Document("$gte", elomin).append("$lte", elomax))))),
                new Document("$group", new Document("_id", "$ECO")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", 10)));

        return results.into(Arrays.asList()).stream().collect(Collectors.toList());
    }

    public int getNumOfWinsAndDrawsPerElo(int elomin, int elomax) {
        Document matchQuery = new Document("$match", new Document("$and", Arrays.asList(
                new Document("whiteElo", new Document("$gte", elomin).append("$lte", elomax)),
                new Document("blackElo", new Document("$gte", elomin).append("$lte", elomax)),
                new Document("result", new Document("$in", Arrays.asList("W", "D"))) // Conta solo vittorie e pareggi
        )));

        Document countQuery = new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", 1)));

        AggregateIterable<Document> result = matchCollection.aggregate(Arrays.asList(matchQuery, countQuery));

        Document countResult = result.first();
        return countResult != null ? countResult.getInteger("total", 0) : 0;
    }

}
