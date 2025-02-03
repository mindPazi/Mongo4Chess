package com.example.demo.dao;

import com.example.demo.model.Admin;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class AdminDAO {

    private final MongoCollection<Document> adminCollection;

    @Autowired
    public AdminDAO(MongoClient mongoClient, Driver neo4jDriver) {
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.adminCollection = database.getCollection("AdminCollection");

    }

    public void updateAdminUsername(String newUsername) {
        adminCollection.updateOne(eq("role", "admin"), new Document("$set", new Document("username", newUsername)));
    }

    public void updateAdminPassword(String newPassword) {
        adminCollection.updateOne(eq("role", "admin"), new Document("$set", new Document("password", newPassword)));
    }

    public Admin getAdmin() {
        Document doc = adminCollection.find(eq("role", "admin")).first();
        return new Admin(doc.getString("username"), doc.getString("password"));
    }
}
