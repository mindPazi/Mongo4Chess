package com.example.demo.DAO;

import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//todo: eliminare la classe
public class MongoDriverUtils {
    /*
     * private static final String PROTOCOL = "mongodb://";
     * private static final String MONGO_HOST = "localhost";
     * private static final String MONGO_PORT = "27017";
     * private static final String MONGO_DB = "nomeNuovoDatabase";
     * private static final String MONGO_USER = "nomeUtente";
     * private static final String MONGO_PSW = "passwordSicura";
     * 
     * @Bean
     * public static MongoClient getConnection() {
     * String connectionString = String.format("%s%s:%s@%s:%s/%s", PROTOCOL,
     * MONGO_USER,
     * MONGO_PSW, MONGO_HOST, MONGO_PORT, MONGO_DB);
     * return MongoClients.create(connectionString);
     * }
     * 
     * public static MongoDatabase getDatabase() {
     * //MongoClient mongoClient = getConnection();
     * return getConnection().getDatabase(MONGO_DB);
     * }
     */

    public static void listCollectionNames(MongoClient myClient, MongoDatabase db) {
        MongoIterable<String> existingCollections = db.listCollectionNames();
        System.out.println("\n////////////// Currently existing databases are: //////////////");
        for (String collection : existingCollections) {
            System.out.println("Collection: " + collection);
        }
        System.out.println("///////////////////////////////////////////////////////////////");
    }

    public static void insertDocuments(MongoCollection<Document> dstCollection, List<Document> documents) {
        String collectionName = dstCollection.getNamespace().getCollectionName();
        System.out.println(
                "\n////////////// Inserting Documents into Collection: [" + collectionName + "] //////////////");
        if (documents.size() == 1) {
            dstCollection.insertOne(documents.get(0));
        }
        if (documents.size() > 1) {
            InsertManyResult insertResults = dstCollection.insertMany(documents);

            List<ObjectId> insertedIds = new ArrayList<>();
            insertResults.getInsertedIds().values()
                    .forEach(doc -> insertedIds.add(doc.asObjectId().getValue()));

            System.out.println("Inserted documents with the following ids: " + insertedIds);
        }
        System.out.println("///////////////////////////////////////////////////////////////");
    }

    public static void insertDocument(MongoCollection<Document> dstCollection, Document document) {
        String collectionName = dstCollection.getNamespace().getCollectionName();
        System.out.println("\n////////////// Inserting Document" +
                " into Collection: [" + collectionName + "] //////////////");
        dstCollection.insertOne(document);

        System.out.println("///////////////////////////////////////////////////////////////");
    }

    public static void deleteDocuments(MongoCollection<Document> dstCollection, Bson deleteFilter) {
        System.out.println("\n////////////// Deleting Documents //////////////");
        DeleteResult deleteResults = dstCollection.deleteMany(deleteFilter);
        String collectionName = dstCollection.getNamespace().getCollectionName();
        System.out.println("Deleted " + deleteResults.getDeletedCount() + " documents from " +
                collectionName + " collection.");
        System.out.println("///////////////////////////////////////////////////////////////");
    }

    public static void deleteDocument(MongoCollection<Document> dstCollection, Bson deleteFilter) {
        System.out.println("\n////////////// Deleting Document //////////////");
        dstCollection.deleteOne(deleteFilter);
        System.out.println("///////////////////////////////////////////////////////////////");
    }

    public static void updateDocument(MongoCollection<Document> dstCollection, Bson filter, Bson update) {
        System.out.println("\n////////////// Updating Documents //////////////");
        dstCollection.updateOne(filter, update);
        System.out.println("///////////////////////////////////////////////////////////////");
    }
}
