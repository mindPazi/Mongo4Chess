package com.example.demo.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.example.demo.DAO.MongoDriverUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
    private static final String PROTOCOL = "mongodb://";
    private static final String MONGO_HOST = "localhost";
    private static final String MONGO_PORT = "27017";
    private static final String MONGO_DB = "chessDB";
    // private static final String MONGO_USER = "user";
    // private static final String MONGO_PSW = "password";

    // private static final String MONGO_DB = "nomeNuovoDatabase";
    // private static final String MONGO_USER = "nomeUtente";
    // private static final String MONGO_PSW = "passwordSicura";

    @Bean
    public MongoClient mongoClient() {
        // Crea una connessione al server MongoDB locale
        // String connectionString = String.format("%s%s:%s@%s:%s/%s", PROTOCOL,
        // MONGO_USER,
        // MONGO_PSW, MONGO_HOST, MONGO_PORT, MONGO_DB);
        // return MongoClients.create(connectionString);
        return MongoClients.create("mongodb://localhost:27017/chessDB");
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase("chessDB");
    }
    /*
     * @Bean
     * public MongoDatabase mongoDatabase(MongoClient mongoClient) {
     * // Ottieni un'istanza del database specificato
     * return mongoClient.getDatabase("my_database");
     * }
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

    /*
     * @Bean
     * public MongoDatabase mongoDatabase() {
     * return MongoDriverUtils.getDatabase();
     * }
     */
}
