package com.example.demo.dao;



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
           "SET a.elo = a.elo + $deltaElo, a.whiteWins = a.whiteWins + $whiteWins, " +
           "a.blackWins = a.blackWins + $blackWins, a.whiteDraws = a.whiteDraws + $whiteDraws, " +
           "a.blackDraws = a.blackDraws + $blackDraws, a.whiteLosses = a.whiteLosses + $whiteLosses, " +
           "a.blackLosses = a.blackLosses + $blackLosses")
    void updatePlayerStats(@Param("username") String username, @Param("deltaElo") int deltaElo,
                           @Param("whiteWins") int whiteWins, @Param("blackWins") int blackWins,
                           @Param("whiteDraws") int whiteDraws, @Param("blackDraws") int blackDraws,
                           @Param("whiteLosses") int whiteLosses, @Param("blackLosses") int blackLosses);


    @Query("USE chessDB " +
           "MATCH (a:PlayerNode {username: $playerId1}), (b:PlayerNode {username: $playerId2}) " +
           "MERGE (a)-[:FRIEND]-(b)")
    void addFriend(String playerId1, String playerId2);

    @Query("USE chessDB " +
           "MATCH (a:PlayerNode {username: $playerId1})-[r:FRIEND]-(b:PlayerNode {username: $playerId2}) " +
           "DELETE r " +
           "RETURN COUNT(r)")
    int removeFriend(String playerId1, String playerId2);

    @Query("USE chessDB " +
           "MATCH (a:PlayerNode {username: $playerId1})-[r:FRIEND]-(b:PlayerNode) " +
           "RETURN b")
    List<PlayerNode> getFriends(String playerId1);

    @Query("""
      USE chessDB
      MATCH (me:PlayerNode {username: $username})
      CALL apoc.path.expandConfig(me, {
            relationshipFilter: "PLAYED",
            minLevel: 2,
            maxLevel: 3,
            uniqueness: "NODE_GLOBAL"
      }) YIELD path
      WITH last(nodes(path)) AS other, me, me.elo AS myElo
      WHERE NOT (me)-[:FRIEND]-(other)
        AND NOT (me)-[:PLAYED]-(other)
        AND abs(other.elo - myElo) < 10
        AND me <> other
      RETURN other.username AS username
      ORDER BY abs(other.elo - myElo) ASC
      LIMIT 5
      """)
    List<String> matchmaking(String username);

    @Query("""
    USE chessDB
    MATCH (n:PlayerNode {username: $username})-[:PLAYED]-(b:PlayerNode)
    WHERE NOT (n)-[:FRIEND]-(b)
    
    MATCH path = (n)-[:FRIEND*1..4]-(b)
    WITH b, path
    ORDER BY length(path) ASC
    WITH b, COLLECT(path)[0] AS shortestPath
    LIMIT 5
    
    UNWIND nodes(shortestPath) AS node
    RETURN node.username AS uname
    """)
    List<String>pathToPlayed(String username);

    @Query("""
    USE chessDB
    MATCH (me:PlayerNode {username: $startingNode}), (target:PlayerNode {username: $endingNode})
    WHERE NOT (me)-[:FRIEND]-(target)
    CALL apoc.path.expandConfig(me, {
        relationshipFilter: "FRIEND",
        minLevel: 1,
        maxLevel: 5,
        uniqueness: "NODE_GLOBAL",
        terminatorNodes: [target],
        bfs: true
        }) YIELD path
    WITH path
    UNWIND nodes(path) AS node
    RETURN node.username AS uname
    """)
    List<String>pathBetween2Player(String startingNode, String endingNode);


    @Query("USE chessDB " + "CREATE (p:PlayerNode {username: $username, elo: $elo, " +
           "blackWins: 0, whiteWins: 0, whiteDraws: 0, blackDraws: 0, " +
           "whiteLosses: 0, blackLosses: 0}) RETURN p")
    void createPlayer(@Param("username") String username, @Param("elo") int elo);

    @Query("USE chessDB " +
           "MATCH (p:PlayerNode {username: $playerId}) DETACH DELETE p")
    void deletePlayer(String playerId);

    @Query("USE chessDB " +
           "MATCH (p:PlayerNode {username: $playerId}) RETURN p")
    PlayerNode getPlayerById(String playerId);

    @Query("USE chessDB " +
           "MATCH (p:PlayerNode {username: $playerId}) RETURN p")
    PlayerNode getStats(String playerId);

    @Query("USE chessDB " +
           "MATCH (p:PlayerNode {username: $playerId}) RETURN p")
    PlayerNode getPlayer(String playerId);

    @Query("USE chessDB " +
           "MATCH (p:PlayerNode {username: $currentUsername}) " +
           "SET p.username = $newUsername")
    void updatePlayerUsername(String currentUsername, String newUsername);

    @Query("USE chessDB " +
           "MATCH (a:PlayerNode {username: $player1}), (b:PlayerNode {username: $player2}) " +
           "MERGE (a)-[:PLAYED]-(b) ")
    void setPlayedEdge(String player1, String player2);
}
