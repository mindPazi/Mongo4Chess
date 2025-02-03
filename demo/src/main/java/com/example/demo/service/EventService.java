package com.example.demo.service;

import com.example.demo.model.EventMongoDB;
import com.example.demo.model.EventNeo4j;
import com.example.demo.repository.mongo.EventMongoDBRepository;
import com.example.demo.repository.neo4j.EventNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class EventService {

    @Autowired
    private EventMongoDBRepository eventMongoDBRepository;

    @Autowired
    private EventNeo4jRepository eventNeo4jRepository;

    // 🔹 Metodi per MongoDB 🔹
    public List<EventMongoDB> getAllEventsFromMongo() {
        return eventMongoDBRepository.findAll();
    }

    public EventMongoDB addEventToMongo(EventMongoDB event) {
        event.setId(UUID.randomUUID().toString()); // Genera un UUID per MongoDB
        return eventMongoDBRepository.save(event);
    }

    public EventMongoDB updateEventInMongo(String id, EventMongoDB updatedEvent) {
        if (!eventMongoDBRepository.existsById(id)) {
            throw new NoSuchElementException("Evento non trovato in MongoDB");
        }
        updatedEvent.setId(id);
        return eventMongoDBRepository.save(updatedEvent);
    }

    public void deleteEventFromMongo(String id) {
        if (!eventMongoDBRepository.existsById(id)) {
            throw new NoSuchElementException("Evento non trovato in MongoDB");
        }
        eventMongoDBRepository.deleteById(id);
    }

    // 🔹 Metodi per Neo4j 🔹
    public List<EventNeo4j> getAllEventsFromNeo4j() {
        return eventNeo4jRepository.findAll();
    }

    public EventNeo4j addEventToNeo4j(EventNeo4j event) {
        return eventNeo4jRepository.save(event);
    }

    public EventNeo4j updateEventInNeo4j(Long id, EventNeo4j updatedEvent) {
        if (!eventNeo4jRepository.existsById(id)) {
            throw new NoSuchElementException("Evento non trovato in Neo4j");
        }
        updatedEvent.setId(id);
        return eventNeo4jRepository.save(updatedEvent);
    }

    public void deleteEventFromNeo4j(Long id) {
        if (!eventNeo4jRepository.existsById(id)) {
            throw new NoSuchElementException("Evento non trovato in Neo4j");
        }
        eventNeo4jRepository.deleteById(id);
    }
}
