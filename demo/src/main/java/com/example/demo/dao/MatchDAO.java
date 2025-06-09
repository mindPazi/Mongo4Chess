package com.example.demo.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Match;
import com.mongodb.client.model.Field;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;

import org.springframework.data.mongodb.core.query.Update;
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

                matchCollection.createIndex(new Document("whiteElo", 1)
                                .append("blackElo", 1)
                                .append("eco", 1));
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
                Query whiteQuery = new Query(Criteria.where("white").is(player));

                Query blackQuery = new Query(Criteria.where("black").is(player));

                List<Match> whiteMatches = mongoTemplate.find(whiteQuery, Match.class, "MatchCollection");
                List<Match> blackMatches = mongoTemplate.find(blackQuery, Match.class, "MatchCollection");

                List<Match> allMatches = new ArrayList<>();
                allMatches.addAll(whiteMatches);
                allMatches.addAll(blackMatches);
                return allMatches;
        }

        public List<Document> getOpeningWithHigherWinRatePerElo(int elomin, int elomax) {
                List<Bson> pipeline = Arrays.asList(

                                Aggregates.match(Filters.and(
                                                Filters.gte("whiteElo", elomin),
                                                Filters.lte("whiteElo", elomax),
                                                Filters.gte("blackElo", elomin),
                                                Filters.lte("blackElo", elomax))),

                                Aggregates.facet(
                                                new Facet("totalGames", Aggregates.count("count")),
                                                new Facet("byEco", Arrays.asList(
                                                                Aggregates.project(Projections.fields(
                                                                                Projections.include("eco", "result"))),
                                                                Aggregates.group("$eco",
                                                                                Accumulators.sum("total", 1),
                                                                                Accumulators.sum("wins", new Document(
                                                                                                "$cond",
                                                                                                Arrays.asList(
                                                                                                                new Document("$in",
                                                                                                                                Arrays.asList("$result",
                                                                                                                                                Arrays.asList("1-0",
                                                                                                                                                                "0-1"))),
                                                                                                                1,
                                                                                                                0))))))),

                                Aggregates.project(new Document()
                                                .append("totalGames",
                                                                new Document("$arrayElemAt",
                                                                                Arrays.asList("$totalGames.count", 0)))
                                                .append("byEco", "$byEco")),

                                Aggregates.unwind("$byEco"),

                                Aggregates.addFields(
                                                new Field<>("eco", "$byEco._id"),
                                                new Field<>("total", "$byEco.total"),
                                                new Field<>("wins", "$byEco.wins"),
                                                new Field<>("percentage",
                                                                new Document("$divide",
                                                                                Arrays.asList("$byEco.total",
                                                                                                "$totalGames")))),

                                Aggregates.match(Filters.gte("percentage", 0.05)),

                                Aggregates.addFields(new Field<>("winRate",
                                                new Document("$divide", Arrays.asList("$wins", "$total")))),

                                Aggregates.project(Projections.fields(
                                                Projections.excludeId(),
                                                Projections.include("eco", "total", "winRate", "percentage"))),

                                Aggregates.sort(Sorts.descending("winRate")),
                                Aggregates.limit(5));

                Bson hint = new Document("whiteElo", 1).append("blackElo", 1).append("eco", 1);
                AggregateIterable<Document> results = matchCollection.aggregate(pipeline).hint(hint);

                List<Document> resultList = new ArrayList<>();
                results.into(resultList);
                return resultList;
        }

        public List<Document> getMostPlayedOpeningsPerElo(int elomin, int elomax) {
                List<Document> pipeline = Arrays.asList(
                                new Document("$match", new Document("$and", Arrays.asList(
                                                new Document("whiteElo",
                                                                new Document("$gte", elomin).append("$lte", elomax)),
                                                new Document("blackElo",
                                                                new Document("$gte", elomin).append("$lte", elomax))))),
                                new Document("$group", new Document("_id", "$eco")
                                                .append("total", new Document("$sum", 1))),
                                new Document("$sort", new Document("total", -1)),
                                new Document("$limit", 10));

                // Ora esegui davvero l'aggregazione
                AggregateIterable<Document> results = matchCollection.aggregate(pipeline)
                                .hint(new Document("whiteElo", 1).append("blackElo", 1).append("eco", 1));

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
                Query query = new Query(
                                Criteria.where("whiteElo").gte(minElo).lte(maxElo)
                                                .and("blackElo").gte(minElo).lte(maxElo))
                                .with(Sort.by(Sort.Direction.DESC, "date"))
                                .limit(10);

                query.withHint(new Document("whiteElo", 1).append("blackElo", 1).append("eco", 1));

                return mongoTemplate.find(query, Match.class, "MatchCollection");
        }

        public List<Document> getNumOfWinsAndDrawsPerElo(int EloMin, int EloMax) {
                // Stage di filtro
                Bson matchStage = Aggregates.match(Filters.and(
                                Filters.gte("whiteElo", EloMin),
                                Filters.lte("whiteElo", EloMax),
                                Filters.gte("blackElo", EloMin),
                                Filters.lte("blackElo", EloMax) // aggiunto per garantire uso indice coprente
                ));

                // Proiezione: solo il campo result (coperto dall'indice)
                Bson preGroupProject = Aggregates.project(Projections.include("result"));

                // Raggruppa per "result" (es. "1-0", "0-1", "1/2-1/2") e conta
                Bson groupStage = Aggregates.group("$result", Accumulators.sum("count", 1));

                // Proietta _id (valore di result) e count
                Bson projectStage = Aggregates.project(Projections.fields(
                                Projections.include("_id", "count")));

                // ✅ Hint sul nuovo indice coprente
                Bson hint = new Document("whiteElo", 1)
                                .append("blackElo", 1).append("eco", 1);

                // Pipeline finale
                List<Bson> pipeline = List.of(matchStage, preGroupProject, groupStage, projectStage);

                // Esecuzione
                AggregateIterable<Document> results = matchCollection.aggregate(pipeline).hint(hint);

                List<Document> resultList = new ArrayList<>();
                results.into(resultList);
                return resultList;
        }

        // public List<Document> getNumOfWinsAndDrawsPerElo(int EloMin, int EloMax) {
        // String EloRange;
        // if (EloMax == Integer.MAX_VALUE) {
        // EloRange = EloMin + "-infinity";
        // } else {
        // EloRange = EloMin + "-" + EloMax;
        // }
        // Bson facetStage = new Document("$facet", new Document()
        // .append(EloRange, Arrays.asList(
        // createMatchStage(EloMin, EloMax),
        // createGroupStage(EloRange))));
        // // ✅ Hint sul nuovo indice coprente
        // Bson hint = new Document("reason", 1)
        // .append("whiteElo", 1)
        // .append("blackElo", 1);
        // AggregateIterable<Document> results =
        // matchCollection.aggregate(Arrays.asList(facetStage)).hint(hint);
        // List<Document> resultList = new ArrayList<>();
        // results.into(resultList);
        // return resultList;
        // }

        private Document createMatchStage(int EloMin, int EloMax) {
                return new Document("$match", new Document("$and", Arrays.asList(
                                new Document("whiteElo", new Document("$gt", EloMin).append("$lte", EloMax)),
                                new Document("blackElo", new Document("$gt", EloMin).append("$lte", EloMax)))));
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
                                                                        new Document("$eq",
                                                                                        Arrays.asList("$result",
                                                                                                        "1-0")),
                                                                        // black won
                                                                        new Document("$eq",
                                                                                        Arrays.asList("$result",
                                                                                                        "0-1")))),
                                                        new Document("$or", Arrays.asList(
                                                                        new Document("$eq", Arrays.asList("$reason",
                                                                                        specificResult)))))),
                                        1,
                                        0)));
                }
        }

}
