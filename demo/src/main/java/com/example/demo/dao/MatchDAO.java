package com.example.demo.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.domain.Sort;
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
    private final MongoTemplate mongoTemplate;

    public MatchDAO(MongoClient mongoClient, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.matchCollection = database.getCollection("MatchCollection");

        // Essential indexes for most frequent queries

        // Ricerca per giocatore (sia come bianco che nero)
        // matchCollection.createIndex(new Document("white", 1));
        // matchCollection.createIndex(new Document("black", 1));

        // // Ricerca per intervallo di date
        // matchCollection.createIndex(new Document("date", 1));

        // Used for the aggregations
        matchCollection.createIndex(new Document("whiteElo", 1));
        matchCollection.createIndex(new Document("blackElo", 1));
    }

    public Match saveMatch(Match match) {
        return mongoTemplate.save(match, "MatchCollection");
    }

    public void deleteAllMatches() {
        matchCollection.deleteMany(new Document());
    }

    public void deleteMatch(String matchId) {
        matchCollection.deleteOne(new Document("_id", matchId));
    }

    public void deleteAllMatchesByPlayer(String player) {
        Document query = new Document("$or", Arrays.asList(
                new Document("white", player),
                new Document("black", player)));
        matchCollection.deleteMany(query);
    }

    public List<Match> getMatches() {
        return mongoTemplate.findAll(Match.class, "MatchCollection");
    }

    public List<Match> getMatchesByPlayer(String player) {
        Query query = new Query(
                new Criteria().orOperator(Criteria.where("white").is(player), Criteria.where("black").is(player)));
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
                new Document("$limit", 5)));

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
        Query query = new Query(Criteria.where("date").gte(startDate).lte(endDate))
                .with(Sort.by(Sort.Direction.DESC, "date")) // Sort by date in descending order
                .limit(10); // Limit the results to 10
        return mongoTemplate.find(query, Match.class, "MatchCollection");
    }

    public List<Match> getMatchesByElo(int minElo, int maxElo) {
        Query query = new Query(Criteria.where("whiteElo").gte(minElo).lte(maxElo)
                .and("blackElo").gte(minElo).lte(maxElo))
                .with(Sort.by(Sort.Direction.DESC, "date")) // Sort by date in descending order
                .limit(10); // Limit the results to 10
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
    }

    public Match getMatch(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Match match = mongoTemplate.findOne(query, Match.class, "MatchCollection");
        if (match == null) {
            throw new RuntimeException("Match not found with id: " + id);
        }
        return match;
    }
}
