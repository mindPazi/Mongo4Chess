package com.example.demo.dao;

import com.example.demo.model.EventMongoDB;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class MongoDBDAO {

    private final MongoCollection<Document> collection;

    @Autowired
    public MongoDBDAO(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.collection = database.getCollection("events");
    }

    public List<EventMongoDB> findAll() {
        List<EventMongoDB> events = new ArrayList<>();
        for (Document doc : collection.find()) {
            events.add(new EventMongoDB(doc.getString("nome"), doc.getString("data")));
        }
        return events;
    }

    public Optional<EventMongoDB> findById(String id) {
        Document doc = collection.find(eq("_id", id)).first();
        return Optional.ofNullable(doc != null ? new EventMongoDB(doc.getString("nome"), doc.getString("data")) : null);
    }

    public EventMongoDB save(EventMongoDB event) {
        Document doc = new Document("_id", event.getId())
                .append("nome", event.getNome())
                .append("data", event.getData());
        collection.insertOne(doc);
        return event;
    }

    public void deleteById(String id) {
        collection.deleteOne(eq("_id", id));
    }
}
