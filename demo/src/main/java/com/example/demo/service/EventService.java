package com.example.demo.service;

import com.example.demo.dao.MongoDBDAO;
import com.example.demo.dao.Neo4jDAO;
import com.example.demo.model.EventMongoDB;
import com.example.demo.model.EventNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventService {

    private final MongoDBDAO mongoDBDAO;
    private final Neo4jDAO neo4jDAO;
    @Value("${spring.neo4j.database}")
    private String database;

    @Autowired
    public EventService(MongoDBDAO mongoDBDAO, Neo4jDAO neo4jDAO) {
        this.mongoDBDAO = mongoDBDAO;
        this.neo4jDAO = neo4jDAO;

    }

    public List<EventMongoDB> getAllEventsFromMongo() {
        return mongoDBDAO.findAll();
    }

    public EventMongoDB addEventToMongo(EventMongoDB event) {
        return mongoDBDAO.save(event);
    }

    public EventMongoDB updateEventInMongo(String id, EventMongoDB updatedEvent) {
        if (mongoDBDAO.findById(id).isEmpty()) {
            throw new NoSuchElementException("Evento non trovato in MongoDB");
        }
        updatedEvent.setId(id);
        return mongoDBDAO.save(updatedEvent);
    }

    public void deleteEventFromMongo(String id) {
        mongoDBDAO.deleteById(id);
    }

    public List<EventNeo4j> getAllEventsFromNeo4j() {
        return neo4jDAO.findAll();
    }

    public EventNeo4j addEventToNeo4j(EventNeo4j event) {
        System.out.println("✅ Database attivo: " + database);
        return neo4jDAO.save(event);
    }

    public EventNeo4j updateEventInNeo4j(Long id, EventNeo4j updatedEvent) {
        if (neo4jDAO.findById(id).isEmpty()) {
            throw new NoSuchElementException("Evento non trovato in Neo4j");
        }
        updatedEvent.setId(id);
        return neo4jDAO.save(updatedEvent);
    }

    public void deleteEventFromNeo4j(Long id) {
        neo4jDAO.deleteById(id);
    }
}
