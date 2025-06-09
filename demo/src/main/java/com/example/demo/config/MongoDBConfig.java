package com.example.demo.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoDBConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;

    // Client di default: usa l'URI con readPreference=secondary (va bene per letture normali)
    @Bean
    @Primary
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri); // contiene readPreference=secondary
    }

    @Primary
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, getDatabaseName());
    }

    @Bean
    public MongoDatabase mongoDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(mongoDatabaseName);
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabaseName;
    }

    /**
     * Transaction manager: usa client con readPreference=primary
     */
    @Bean(name = "mongoTransactionManager")
    public MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory mongoDbFactory) {
        return new MongoTransactionManager(mongoDbFactory);
    }

    /**
     * MongoTemplate per forzare letture da primario (es. letture in transazione)
     */
    @Bean(name = "primaryReadMongoTemplate")
    public MongoTemplate primaryReadMongoTemplate() {
        ConnectionString connString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .readPreference(ReadPreference.primary()) // sovrascrive solo qui
                .build();

        MongoClient client = MongoClients.create(settings);
        return new MongoTemplate(client, mongoDatabaseName);
    }
}
