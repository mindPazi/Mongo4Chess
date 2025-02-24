package com.example.demo.dao;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.PlayerNode;

import java.util.List;

@Repository
public interface PlayerNodeDAO extends Neo4jRepository<PlayerNode, Long> {

        @Query("USE chessDB " +
                        "MATCH (a:PlayerNode {id: $playerId1}), (b:PlayerNode {id: $playerId2}) " +
                        "MERGE (a)-[:FRIENDS_WITH]->(b)")
        void addFriend(String playerId1, String playerId2);

        @Query("USE chessDB " +
                        "MATCH (a:PlayerNode {id: $playerId1})-[r:FRIENDS_WITH]->(b:PlayerNode {id: $playerId2}) " +
                        "DELETE r " +
                        "RETURN COUNT(r)")
        int removeFriend(String playerId1, String playerId2);

        @Query("USE chessDB " +
                        "MATCH (a:PlayerNode {id: $playerId1})-[r:FRIENDS_WITH]->(b:PlayerNode) " +
                        "RETURN b")
        List<PlayerNode> getFriends(String playerId1);

        @Query("USE chessDB " +
                        "CREATE (p:PlayerNode {id: $id, username: $username, elo: $elo, " +
                        "blackWins: 0, whiteWins: 0, whiteDraws: 0, blackDraws: 0, " +
                        "whiteLosses: 0, blackLosses: 0, isBanned: false})")
        void addPlayer(String id, String username, int elo);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {id: $playerId}) DETACH DELETE p")
        void deletePlayer(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {id: $playerId}) SET p.isBanned = true")
        void banPlayer(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {id: $playerId}) SET p.isBanned = false")
        void unbanPlayer(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {id: $playerId}) RETURN p")
        PlayerNode getPlayerById(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode) RETURN p")
        List<PlayerNode> getAllPlayers();

        @Query("USE chessDB " + "CREATE (p:PlayerNode {username: $username, elo: $elo, " +
                        "blackWins: 0, whiteWins: 0, whiteDraws: 0, blackDraws: 0, " +
                        "whiteLosses: 0, blackLosses: 0, isBanned: false}) RETURN p")
        PlayerNode createPlayer(@Param("username") String username, @Param("elo") int elo);

}
