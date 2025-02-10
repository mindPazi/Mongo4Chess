package com.example.demo.dao;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.PlayerNode;

import java.util.List;

@Repository
public interface PlayerNodeDAO extends Neo4jRepository<PlayerNode, String> {

        @Query("MATCH (a:PlayerNode {id: $playerId1}), (b:PlayerNode {id: $playerId2}) " +
                        "MERGE (a)-[:FRIENDS_WITH]->(b)")
        public void addFriend(String playerId1, String playerId2);

        @Query("MATCH (a:PlayerNode {id: $playerId1})-[r:FRIENDS_WITH]->(b:PlayerNode {id: $playerId2}) " +
                        "DELETE r")
        public void removeFriend(String playerId1, String playerId2);

        @Query("MATCH (a:PlayerNode {id: $playerId1})-[r:FRIENDS_WITH]->(b:PlayerNode) " +
                        "RETURN b")
        public List<PlayerNode> getFriends(String playerId1);
}
