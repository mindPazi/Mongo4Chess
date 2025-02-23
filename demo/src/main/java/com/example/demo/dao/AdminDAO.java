package com.example.demo.dao;

import com.example.demo.model.Admin;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class AdminDAO {

    private final MongoCollection<Document> adminCollection;

    @Autowired
    public AdminDAO(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("chessDB");
        this.adminCollection = database.getCollection("AdminCollection");
    }

    public void updateAdminUsername(String oldUsername, String newUsername) {
        adminCollection.updateOne(eq("username", oldUsername),
                new Document("$set", new Document("username", newUsername)));
    }

    public void updateAdminPassword(String username, String newPassword) {
        adminCollection.updateOne(eq("username", username),
                new Document("$set", new Document("password", newPassword)));
    }

    public Admin getAdmin(String username) {
        Document doc = adminCollection.find(eq("username", username)).first();
        return doc != null ? new Admin(doc.getString("username"), doc.getString("password")) : null;
    }

}
