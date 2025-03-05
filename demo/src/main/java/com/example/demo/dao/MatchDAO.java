package com.example.demo.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Match;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MatchDAO {
    private final MongoCollection<Document> matchCollection;
    private final MongoCollection<Document> playerCollection;

    public MatchDAO(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.matchCollection = database.getCollection("MatchCollection");
        this.playerCollection = database.getCollection("PlayerCollection");
    }

    public void saveMatch(Match match) {
        Document matchDocument = Document.parse(match.toJson());
        matchCollection.insertOne(matchDocument);
        playerCollection.updateOne(new Document("username", match.getWhite()),
                new Document("$push", new Document("matches", convertMatchToDocumentForPlayer(match, match.getWhite()))));
        playerCollection.updateOne(new Document("username", match.getBlack()),
                new Document("$push", new Document("matches", convertMatchToDocumentForPlayer(match, match.getBlack()))));
    }

    private Document convertMatchToDocumentForPlayer(Match match, String username) {
        if (match.getWhite().equals(username)) {
            return new Document("Elo", match.getWhiteElo())
                    .append("date", match.getDate());
        }
        return new Document("Elo", match.getBlackElo())
                .append("date", match.getDate());
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
                        new Document("whiteElo",
                                new Document("$gte", elomin).append("$lte", elomax)),
                        new Document("blackElo",
                                new Document("$gte", elomin).append("$lte", elomax))))),
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
                new Document("result", new Document("$in", Arrays.asList("W", "D"))) // Conta solo
                // vittorie e
                // pareggi
        )));

        Document countQuery = new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", 1)));

        AggregateIterable<Document> result = matchCollection.aggregate(Arrays.asList(matchQuery, countQuery));

        Document countResult = result.first();
        return countResult != null ? countResult.getInteger("total", 0) : 0;
    }

    public List<Document> getMatches() {
        return matchCollection.find().into(Arrays.asList());
    }

    public List<Document> getMatchesByPlayer(String player) {
        Document query = new Document("$or", Arrays.asList(
                new Document("white", player),
                new Document("black", player)));
        return matchCollection.find(query).into(Arrays.asList());
    }

    public Document getOpeningWithHigherWinRatePerElo(int elomin, int elomax) {
        AggregateIterable<Document> results = matchCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("$and", Arrays.asList(
                        new Document("whiteElo",
                                new Document("$gte", elomin).append("$lte", elomax)),
                        new Document("blackElo",
                                new Document("$gte", elomin).append("$lte", elomax))))),
                new Document("$group", new Document("_id", "$ECO")
                        .append("total", new Document("$sum", 1))
                        .append("wins", new Document("$sum",
                                new Document("$cond", Arrays.asList(
                                        new Document("$eq",
                                                Arrays.asList("$result",
                                                        "W")),
                                        1, 0))))),
                new Document("$project", new Document("_id", 0)
                        .append("ECO", "$_id")
                        .append("total", 1)
                        .append("winRate",
                                new Document("$divide",
                                        Arrays.asList("$wins", "$total")))),
                new Document("$sort", new Document("winRate", -1)),
                new Document("$limit", 1)));

        return results.first();
    }

    public List<Document> getMostPlayedOpeningsPerElo(int elomin, int elomax) {
        AggregateIterable<Document> results = matchCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("$and", Arrays.asList(
                        new Document("whiteElo",
                                new Document("$gte", elomin).append("$lte", elomax)),
                        new Document("blackElo",
                                new Document("$gte", elomin).append("$lte", elomax))))),
                new Document("$group", new Document("_id", "$ECO")
                        .append("total", new Document("$sum", 1))),
                new Document("$sort", new Document("total", -1)),
                new Document("$limit", 10)));

        return results.into(Arrays.asList()).stream().collect(Collectors.toList());
    }

    public List<Document> executeAggregation(List<Document> pipeline) {
        AggregateIterable<Document> results = matchCollection.aggregate(pipeline);
        return results.into(Arrays.asList()).stream().collect(Collectors.toList());
    }

    // Metodo per convertire un oggetto Match in un documento MongoDB
    private Document convertMatchToDocument(Match match) {
        Document doc = new Document();
        doc.append("id", match.getId());
        doc.append("date", match.getDate());
        doc.append("white", match.getWhite());
        doc.append("black", match.getBlack());
        doc.append("result", match.getResult());
        doc.append("whiteElo", match.getWhiteElo());
        doc.append("blackElo", match.getBlackElo());
        doc.append("timeControl", match.getTimeControl());
        doc.append("ECO", match.getEco());
        doc.append("plyCount", match.getPlyCount());
        doc.append("reason", match.getReason());
        doc.append("moves", match.getMoves());
        return doc;
    }

    // Metodo per convertire un documento MongoDB in un oggetto Match
    private static Match convertDocumentToMatch(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Match(
                doc.getString("id"),
                doc.getDate("date"),
                doc.getString("white"),
                doc.getString("black"),
                doc.getString("result"),
                doc.getInteger("whiteElo", 0),
                doc.getInteger("blackElo", 0),
                doc.getString("timeControl"),
                doc.getString("ECO"),
                doc.getInteger("plyCount", 0),
                doc.getString("reason"),
                doc.getList("moves", String.class));
    }

    // Metodo per convertire l'oggetto in JSON
    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Errore nella conversione di Match in JSON", e);
        }
    }

    public void deleteMatchesBeforeDate(Date date) {
        Document query = new Document("date", new Document("$lt", date));
        matchCollection.deleteMany(query);
    }

    public void addMatches(List<Match> matches) {
        List<Document> matchDocuments = matches.stream()
                .map(Match::toJson)
                .map(Document::parse)
                .collect(Collectors.toList());
        matchCollection.insertMany(matchDocuments);
        for (Match match : matches) {
            playerCollection.updateOne(new Document("username", match.getWhite()),
                    new Document("$push", new Document("matches", convertMatchToDocumentForPlayer(match, match.getWhite()))));
            playerCollection.updateOne(new Document("username", match.getBlack()),
                    new Document("$push", new Document("matches", convertMatchToDocumentForPlayer(match, match.getBlack()))));
        }
    }
}
