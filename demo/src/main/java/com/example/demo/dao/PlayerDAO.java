package com.example.demo.dao;

import com.example.demo.model.*;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Repository
public class PlayerDAO {
    private final MongoCollection<Document> playerCollection;
    private final MongoTemplate mongoTemplate;

    public PlayerDAO(MongoClient mongoclient, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        MongoDatabase mongodatabase = mongoclient.getDatabase("chessDB");
        this.playerCollection = mongodatabase.getCollection("PlayerCollection");
        // Unique index on username
        this.playerCollection.createIndex(
                new Document("username", 1),
                new com.mongodb.client.model.IndexOptions().unique(true));
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
                new Document("$unset", new Document("isBanned", "")));
    }

    public void deletePlayer(String username) {
        long deletedCount = playerCollection.deleteOne(new Document("username", username)).getDeletedCount();
        if (deletedCount == 0) {
            throw new RuntimeException("Giocatore non trovato con username: " + username);
        }
    }

    public void updatePlayerPassword(String username, String newPassword) {
        playerCollection.updateOne(new Document("username", username),
                new Document("$set", new Document("password", newPassword)));
    }

    public void updatePlayerUsername(String oldUsername, String newUsername) {
        playerCollection.updateOne(
                new Document("username", oldUsername),
                new Document("$set", new Document("username", newUsername)));
    }

    public List<PlayerMatch> getEloTrend(String username) {
        // Get the player first
        Player player = getPlayer(username);

        if (player == null) {
            throw new RuntimeException("Player not found: " + username);
        }

        // return the list of the elos with the dates
        return player.getMatches();
    }

    public Player getPlayer(String player) {
        List<Document> results = playerCollection.find(new Document("username", player)).into(new ArrayList<>());
        if (results.isEmpty()) {
            return null; // player not found
        }
        return convertDocumentToPlayer(results.get(0));
    }

    @SuppressWarnings("unchecked")
    private Player convertDocumentToPlayer(Document doc) {
        Player player = new Player();
        player.setUsername(doc.getString("username"));
        player.setPassword(doc.getString("password"));
        player.setIsBanned(doc.getBoolean("isBanned", false));
        List<Document> matchDocs = (List<Document>) doc.get("matches");
        List<PlayerMatch> matches = new ArrayList<>();
        if (matchDocs != null) {
            for (Document matchDoc : matchDocs) {
                PlayerMatch match = new PlayerMatch();
                match.set_id(matchDoc.getObjectId("_id"));
                match.setElo(matchDoc.getInteger("elo"));
                match.setDate(matchDoc.getDate("date"));
                matches.add(match);
            }
        }
        player.setMatches(matches);

        List<Document> tournamentDocs = (List<Document>) doc.get("tournaments");
        List<PlayerTournament> tournaments = new ArrayList<>();
        if (tournamentDocs != null) {
            for (Document tournamentDoc : tournamentDocs) {
                tournaments.add(new PlayerTournament(
                        tournamentDoc.getString("id"),
                        tournamentDoc.getString("name"),
                        tournamentDoc.getDate("startDate"),
                        tournamentDoc.getDate("endDate"),
                        tournamentDoc.getInteger("position")));
            }
        }
        player.setTournaments(tournaments);

        return player;
    }

    public List<PlayerTournament> getMyTournaments(String playerId) {
        Player player = getPlayer(playerId);
        if (player == null) {
            throw new RuntimeException("Player not found with username: " + playerId);
        }
        return player.getTournaments();
    }

    public void addTournament(String playerId, PlayerTournament playerTournament) {
        Document tournamentDoc = new Document("name", playerTournament.getName())
                .append("id", playerTournament.getId())
                .append("startDate", playerTournament.getStartDate())
                .append("endDate", playerTournament.getEndDate())
                .append("position", playerTournament.getPosition());
        playerCollection.updateOne(new Document("username", playerId),
                new Document("$push", new Document("tournaments", tournamentDoc)));
    }

    public void removeTournament(String tournamentId, String playerId) {
        playerCollection.updateOne(new Document("username", playerId),
                new Document("$pull", new Document("tournaments", new Document("id", tournamentId))));

    }

    public void updateTournamentPositions(List<TournamentPlayer> tournamentPlayers, String tournamentId) {
        for (TournamentPlayer tournamentPlayer : tournamentPlayers) {
            playerCollection.updateOne(new Document("username", tournamentPlayer.getUsername())
                            .append("tournaments.id", tournamentId),
                    new Document("$set", new Document("tournaments.$.position", tournamentPlayer.getPosition())));
        }
    }


    public void rollbackPlayer(Player player) {
        if (player == null || player.getUsername() == null) {
            throw new IllegalArgumentException("Player o username nullo!");
        }
        Document doc = new Document("username", player.getUsername())
                .append("password", player.getPassword());

        if (player.getMatches() != null && !player.getMatches().isEmpty()) {
            doc.append("matches", player.getMatches());
        }
        if (player.getTournaments() != null && !player.getTournaments().isEmpty()) {
            doc.append("tournaments", player.getTournaments());
        }
        if (player.getIsBanned() != null) {
            doc.append("isBanned", player.getIsBanned());
        }

        playerCollection.replaceOne(
                new Document("username", player.getUsername()),
                doc,
                new com.mongodb.client.model.ReplaceOptions().upsert(true));
    }

    public void addMatch(String player, PlayerMatch playerMatch) {
        Query query = new Query(Criteria.where("username").is(player));
        Update update = new Update().push("matches", playerMatch);
        mongoTemplate.updateFirst(query, update, "PlayerCollection");
    }

    public void removeMatch(String player, ObjectId id) {
        // Remove the match from the player's match list
        playerCollection.updateOne(
                new Document("username", player),
                new Document("$pull", new Document("matches", new Document("_id", id)))
        );
    }

    public void deleteAllMatches() {
        playerCollection.updateMany(new Document(),
                new Document("$set", new Document("matches", new ArrayList<>())));
    }

    public void deleteAllMatchesByPlayer(String player) {
        playerCollection.updateOne(new Document("username", player),
                new Document("$set", new Document("matches", new ArrayList<>())));
    }

    public void deleteMatchesBeforeDate(Date date) {
        playerCollection.updateMany(
                new Document("matches.date", new Document("$lt", date)),
                new Document("$pull", new Document("matches",
                        new Document("date", new Document("$lt", date)))));
    }
}
