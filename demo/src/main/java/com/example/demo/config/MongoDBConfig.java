package com.example.demo.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager; // Importa questa classe
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory; // Importa questa classe
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement // Abilita la gestione delle transazioni
@Configuration
public class MongoDBConfig extends AbstractMongoClientConfiguration{

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    // Nome del database MongoDB
    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(mongoDatabaseName);
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabaseName;
    }

    //todo: testare @transactional una volta fatto il deploy sulle macchine virtuali
//    @Bean(name = "mongoTransactionManager")
//    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
//        return new MongoTransactionManager(dbFactory);
//    }
}