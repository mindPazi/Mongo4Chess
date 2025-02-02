package com.example.demo.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class Neo4JConfig {

    @Bean
    public Driver neo4jDriver() {
        String uri = "neo4j://localhost:7687";
        String user = "neo4j";
        String password = System.getenv("NEO4J_PSW");

        Config config = Config.builder()
                .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
                .withMaxConnectionPoolSize(50)
                .withConnectionAcquisitionTimeout(2, TimeUnit.MINUTES)
                .withMaxTransactionRetryTime(1, TimeUnit.MINUTES)
                .build();

        return GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
    }
}