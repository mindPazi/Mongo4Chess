package com.example.demo.repository.neo4j;

import com.example.demo.model.EventNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventNeo4jRepository extends Neo4jRepository<EventNeo4j, Long> {
}
