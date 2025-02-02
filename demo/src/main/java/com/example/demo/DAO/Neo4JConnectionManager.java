package com.example.demo.DAO;

import lombok.Getter;
import org.neo4j.driver.*;
import java.util.concurrent.TimeUnit;

//todo spostare tutto in neo4jconfig
@Getter
public class Neo4JConnectionManager implements AutoCloseable {

    /**
     * -- GETTER --
     * Returns the Neo4j driver instance.
     *
     * @return The Neo4j driver.
     */
    private final Driver driver;

    /**
     * Constructor that initializes the connection to the Neo4j database.
     *
     * @param uri      The connection URI for the Neo4j database (e.g.,
     *                 "neo4j://localhost:7687").
     * @param user     The username for authentication.
     * @param password The password for authentication.
     */
    public Neo4JConnectionManager(String uri, String user, String password) {
        // Configure connection pool and authentication settings
        Config config = Config.builder()
                .withMaxConnectionLifetime(30, TimeUnit.MINUTES) // Maximum lifetime of a connection in the pool
                .withMaxConnectionPoolSize(50) // Maximum number of connections in the pool
                .withConnectionAcquisitionTimeout(2, TimeUnit.MINUTES) // Timeout for acquiring a connection
                .withMaxTransactionRetryTime(1, TimeUnit.MINUTES) // Maximum time to retry a transaction
                .build();

        // Initialize the driver with authentication and configuration
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
    }

    /**
     * Closes the connection to the Neo4j database.
     */
    public void close() {
        driver.close();
    }

    /**
     * Demonstrates the connection and performs a simple query to validate the
     * connection.
     */
    public void demonstrateConnection() {
        try (Session session = driver.session()) {
            String query = "MATCH (n) RETURN COUNT(n) AS NodeCount";
            Result result = session.run(query);
            long nodeCount = result.single().get("NodeCount").asLong();
            System.out.println("Successfully connected to the database. Node count: " + nodeCount);
        } catch (Exception e) {
            System.err.println("An error occurred while interacting with the database: " + e.getMessage());
        }
    }
}
