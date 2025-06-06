package com.example.demo.config;

import org.neo4j.driver.Driver; // Importa il driver Neo4j
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager; // Importa la classe del transaction manager
import org.springframework.transaction.PlatformTransactionManager; // Importa l'interfaccia PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement; // Abilita la gestione delle transazioni

@Configuration
@EnableTransactionManagement // Abilita la gestione delle transazioni
public class Neo4jConfig {

    /**
     * Definisce il Neo4jTransactionManager con un nome specifico.
     * Spring Boot auto-configura il Driver di Neo4j se le proprietà di connessione sono impostate.
     * Questo metodo riceverà automaticamente il Driver iniettato.
     */
    @Bean(name = "neo4jTransactionManager") // Il nome che vuoi assegnare al tuo transaction manager Neo4j
    public PlatformTransactionManager neo4jTransactionManager(Driver driver) {
        // Il Neo4jTransactionManager ha bisogno del Driver di Neo4j per operare.
        // Spring Boot lo fornirà automaticamente se è configurato.
        return new Neo4jTransactionManager(driver);
    }
}

