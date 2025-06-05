package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
//todo: vedere se transactional funziona per mongo sulle repliche
//todo: discutere dello sharding/replication
//todo: quando gabri ha messo l'id nel playerMatch, sistemare il rollback del match
//todo: popolare la tournament collection
//todo: testare le due query su grafo!!!
//todo: indici sul grafo?
//todo: verificare l'efficienza degli indici
@EnableMongoRepositories(basePackages = "com.example.demo.repository.mongo")
@EnableNeo4jRepositories(basePackages = { "com.example.demo.repository.neo4j", "com.example.demo.dao" })

@SpringBootApplication
public class DemoApplication {
	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		logger.info("\nApplication is running...");
		logger.info("-------------------------------------");
	}
}
