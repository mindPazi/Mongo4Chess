package com.example.demo.dao;

import com.example.demo.model.PlayerMatch;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Match;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MatchDAO {
    private final MongoCollection<Document> matchCollection;
    private final MongoCollection<Document> playerCollection;
    private final MongoTemplate mongoTemplate;

    public MatchDAO(MongoClient mongoClient, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.matchCollection = database.getCollection("MatchCollection");
        this.playerCollection = database.getCollection("PlayerCollection");
    }

    public void saveMatch(Match match) {
        //Document matchDocument = Document.parse(match.toJson());
        //matchCollection.insertOne(matchDocument);
        mongoTemplate.save(match, "MatchCollection");
        PlayerMatch whitePlayerMatch = new PlayerMatch(match.getWhiteElo(), match.getDate());
        PlayerMatch blackPlayerMatch = new PlayerMatch(match.getBlackElo(), match.getDate());

        mongoTemplate.updateFirst(
                new Query(Criteria.where("username").is(match.getWhite())),
                new Update().push("matches", whitePlayerMatch),
                "PlayerCollection"
        );

        mongoTemplate.updateFirst(
                new Query(Criteria.where("username").is(match.getBlack())),
                new Update().push("matches", blackPlayerMatch),
                "PlayerCollection"
        );

//        playerCollection.updateOne(new Document("username", match.getWhite()),
//                new Document("$push", new Document("matches", convertMatchToDocumentForPlayer(match, match.getWhite()))));
//        playerCollection.updateOne(new Document("username", match.getBlack()),
//                new Document("$push", new Document("matches", convertMatchToDocumentForPlayer(match, match.getBlack()))));
    }

    private Document convertMatchToDocumentForPlayer(Match match, String username) {
        if (match.getWhite().equals(username)) {
            return new Document("elo", match.getWhiteElo())
                    .append("date", match.getDate());
        }
        return new Document("elo", match.getBlackElo())
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

//    public int getNumOfWinsAndDrawsPerElo(int elomin, int elomax) {
//        Document matchQuery = new Document("$match", new Document("$and", Arrays.asList(
//                new Document("whiteElo", new Document("$gte", elomin).append("$lte", elomax)),
//                new Document("blackElo", new Document("$gte", elomin).append("$lte", elomax)),
//                new Document("result", new Document("$in", Arrays.asList("W", "D"))) // Conta solo
//                // vittorie e
//                // pareggi
//        )));
//
//        Document countQuery = new Document("$group", new Document("_id", null)
//                .append("total", new Document("$sum", 1)));
//
//        AggregateIterable<Document> result = matchCollection.aggregate(Arrays.asList(matchQuery, countQuery));
//
//        Document countResult = result.first();
//        return countResult != null ? countResult.getInteger("total", 0) : 0;
//    }

//    public List<Document> getMatches() {
//        return matchCollection.find().into(Arrays.asList());
//    }

    public List<Match> getMatches() {
        return mongoTemplate.findAll(Match.class, "MatchCollection");
    }


    public List<Match> getMatchesByPlayer(String player) {
        Query query = new Query(new Criteria().orOperator(Criteria.where("white").is(player), Criteria.where("black").is(player)));
        return mongoTemplate.find(query, Match.class, "MatchCollection");
    }

    public Document getOpeningWithHigherWinRatePerElo(int elomin, int elomax) {
        AggregateIterable<Document> results = matchCollection.aggregate(Arrays.asList(
                new Document("$match", new Document("$and", Arrays.asList(
                        new Document("whiteElo",
                                new Document("$gte", elomin).append("$lte", elomax)),
                        new Document("blackElo",
                                new Document("$gte", elomin).append("$lte", elomax))))),
                new Document("$group", new Document("_id", "$eco")
                        .append("total", new Document("$sum", 1))
                        .append("wins", new Document("$sum",
                                new Document("$cond", Arrays.asList(
                                        new Document("$eq",
                                                Arrays.asList("$result",
                                                        "1-0")),
                                        1, 0))))),
                new Document("$project", new Document("_id", 0)
                        .append("eco", "$_id")
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
                new Document("$group", new Document("_id", "$eco")
                        .append("total", new Document("$sum", 1))),
                new Document("$sort", new Document("total", -1)),
                new Document("$limit", 10)));

        List<Document> resultList = new ArrayList<>();
        results.into(resultList);
        return resultList;
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
        Query query = new Query(Criteria.where("date").lt(date));
        mongoTemplate.remove(query, "MatchCollection");
    }

    public void updatePlayerUsernameInMatches(String currentUsername, String newUsername) {
        Query query = new Query(new Criteria().orOperator(
                Criteria.where("white").is(currentUsername),
                Criteria.where("black").is(currentUsername)));
        Update update = new Update().set("white", newUsername).set("black", newUsername);
        mongoTemplate.updateMulti(query, update, "MatchCollection");
    }

    public List<Match> getMatchesByDate(Date startDate, Date endDate) {
        Query query = new Query(Criteria.where("date").gte(startDate).lte(endDate));
        return mongoTemplate.find(query, Match.class, "MatchCollection");
    }

    public List<Match> getMatchesByElo(int minElo, int maxElo) {
        Query query = new Query(Criteria.where("whiteElo").gte(minElo).lte(maxElo)
                .and("blackElo").gte(minElo).lte(maxElo));
        return mongoTemplate.find(query, Match.class, "MatchCollection");
    }

    private Document createGroupStage(String id) {
        return new Document("$group", new Document("_id", id)
                .append("wins_checkmated", createSumCondition("win", "checkmated"))
                .append("wins_resigned", createSumCondition("win", "resigned"))
                .append("wins_timeout", createSumCondition("win", "timeout"))
                .append("wins_abandoned", createSumCondition("win", "abandoned"))
                .append("draws_timevsinsufficient", createSumCondition("draw", "timevsinsufficient"))
                .append("draws_repetition", createSumCondition("draw", "repetition"))
                .append("draws_insufficient", createSumCondition("draw", "insufficient"))
                .append("draws_stalemate", createSumCondition("draw", "stalemate"))
                .append("draws_agreed", createSumCondition("draw", "agreed")));
    }

    private Document createSumCondition(String result, String specificResult) {
        if (Objects.equals(result, "draw")) {
            return new Document("$sum", new Document("$cond", Arrays.asList(
                    new Document("$and", Arrays.asList(
                            new Document("$eq", Arrays.asList("$result", "1/2-1/2")),
                            new Document("$eq", Arrays.asList("$reason", specificResult)))),
                    1,
                    0)));
        } else {
            return new Document("$sum", new Document("$cond", Arrays.asList(
                    new Document("$and", Arrays.asList(
                            new Document("$or", Arrays.asList(
                                    // white won
                                    new Document("$eq", Arrays.asList("$result", "1-0")),
                                    // black won
                                    new Document("$eq", Arrays.asList("$result", "0-1")))),
                            new Document("$or", Arrays.asList(
                                    new Document("$eq", Arrays.asList("$reason", specificResult)))))),
                    1,
                    0)));
        }
    }

    private Document createMatchStage(int EloMin, int EloMax) {
        return new Document("$match", new Document("$and", Arrays.asList(
                new Document("whiteElo", new Document("$gt", EloMin).append("$lte", EloMax)),
                new Document("blackElo", new Document("$gt", EloMin).append("$lte", EloMax)))));
    }

    public List<Document> getNumOfWinsAndDrawsPerElo(int EloMin, int EloMax) {
        String EloRange;
        if (EloMax == Integer.MAX_VALUE) {
            EloRange = EloMin + "-infinity";
        } else {
            EloRange = EloMin + "-" + EloMax;
        }
        Bson facetStage = new Document("$facet", new Document()
                .append(EloRange, Arrays.asList(
                        createMatchStage(EloMin, EloMax),
                        createGroupStage(EloRange))));
        AggregateIterable<Document> results = matchCollection.aggregate(Arrays.asList(facetStage));
        List<Document> resultList = new ArrayList<>();
        results.into(resultList);
        return resultList;
        //return matchDAO.executeAggregation(Arrays.asList(facetStage));
    }

}
