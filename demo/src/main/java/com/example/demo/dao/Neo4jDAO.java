package com.example.demo.dao;

import com.example.demo.model.EventNeo4j;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.neo4j.driver.Record;

@Repository
public class Neo4jDAO {

    private final Driver driver;

    @Autowired
    public Neo4jDAO(Driver driver) {
        this.driver = driver;
    }

    public List<EventNeo4j> findAll() {
        List<EventNeo4j> events = new ArrayList<>();
        try (Session session = driver.session()) {
            session.run("MATCH (e:Event) RETURN e.id, e.nome, e.data")
                    .stream()
                    .forEach(record -> events.add(new EventNeo4j(
                            record.get("e.nome").asString(),
                            record.get("e.data").asString())));
        }
        return events;
    }

    public Optional<EventNeo4j> findById(Long id) {
        try (Session session = driver.session()) {
            Record record = session.run("MATCH (e:Event) WHERE e.id = $id RETURN e.nome, e.data",
                    Values.parameters("id", id)).single();
            if (record != null) {
                return Optional.of(new EventNeo4j(record.get("e.nome").asString(), record.get("e.data").asString()));
            }
        }
        return Optional.empty();
    }

    public EventNeo4j save(EventNeo4j event) {
        try (Session session = driver.session()) {
            session.run("CREATE (e:Event {id: $id, nome: $nome, data: $data})",
                    Values.parameters("id", event.getId(), "nome", event.getNome(), "data", event.getData()));
        }
        return event;
    }

    public void deleteById(Long id) {
        try (Session session = driver.session()) {
            session.run("MATCH (e:Event) WHERE e.id = $id DELETE e", Values.parameters("id", id));
        }
    }
}
