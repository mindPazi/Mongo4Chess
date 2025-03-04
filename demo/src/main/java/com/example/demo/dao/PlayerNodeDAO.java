package com.example.demo.dao;

import com.example.demo.model.Player;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.PlayerNode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerNodeDAO extends Neo4jRepository<PlayerNode, UUID> {

        @Query("USE chessDB MATCH (p:PlayerNode {username: $username}) RETURN p.elo")
        Optional<Integer> getElo(@Param("username") String username);

        @Query("USE chessDB MATCH (a:PlayerNode {username: $username}) " +
               "SET a.elo = $elo, a.whiteWins = a.whiteWins + $whiteWins, " +
               "a.blackWins = a.blackWins + $blackWins, a.whiteDraws = a.whiteDraws + $whiteDraws, " +
               "a.blackDraws = a.blackDraws + $blackDraws, a.whiteLosses = a.whiteLosses + $whiteLosses, " +
               "a.blackLosses = a.blackLosses + $blackLosses")
        void updatePlayerStats(@Param("username") String username, @Param("elo") int elo,
                               @Param("whiteWins") int whiteWins, @Param("blackWins") int blackWins,
                               @Param("whiteDraws") int whiteDraws, @Param("blackDraws") int blackDraws,
                               @Param("whiteLosses") int whiteLosses, @Param("blackLosses") int blackLosses);



        @Query("USE chessDB " +
                        "MATCH (a:PlayerNode {username: $playerId1}), (b:PlayerNode {username: $playerId2}) " +
                        "MERGE (a)-[:FRIENDS_WITH]->(b)")
        void addFriend(String playerId1, String playerId2);

        @Query("USE chessDB " +
                        "MATCH (a:PlayerNode {username: $playerId1})-[r:FRIENDS_WITH]->(b:PlayerNode {username: $playerId2}) " +
                        "DELETE r " +
                        "RETURN COUNT(r)")
        int removeFriend(String playerId1, String playerId2);

        @Query("USE chessDB " +
                        "MATCH (a:PlayerNode {username: $playerId1})-[r:FRIENDS_WITH]->(b:PlayerNode) " +
                        "RETURN b")
        List<PlayerNode> getFriends(String playerId1);

        /*@Query("USE chessDB " +
                        "CREATE (p:PlayerNode {username: $id, username: $username, elo: $elo, " +
                        "blackWins: 0, whiteWins: 0, whiteDraws: 0, blackDraws: 0, " +
                        "whiteLosses: 0, blackLosses: 0, isBanned: false})")
        void addPlayer(String id, String username, int elo);*/

        @Query("USE chessDB " + "CREATE (p:PlayerNode {username: $username, elo: $elo, " +
               "blackWins: 0, whiteWins: 0, whiteDraws: 0, blackDraws: 0, " +
               "whiteLosses: 0, blackLosses: 0}) RETURN p")
        void createPlayer(@Param("username") String username, @Param("elo") int elo);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {username: $playerId}) DETACH DELETE p")
        void deletePlayer(String playerId);

        //il campo ban è solo su mongo
//        @Query("USE chessDB " +
//                        "MATCH (p:PlayerNode {username: $playerId}) SET p.isBanned = true")
//        void banPlayer(String playerId);
//
//        @Query("USE chessDB " +
//                        "MATCH (p:PlayerNode {username: $playerId}) SET p.isBanned = false")
//        void unbanPlayer(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {username: $playerId}) RETURN p")
        PlayerNode getPlayerById(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode) RETURN p")
        List<PlayerNode> getAllPlayers();

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {username: $playerId}) RETURN p")
        PlayerNode getStats(String playerId);

        @Query("USE chessDB " +
                        "MATCH (p:PlayerNode {username: $playerId}) RETURN p")
        PlayerNode getPlayer(String playerId);
}
