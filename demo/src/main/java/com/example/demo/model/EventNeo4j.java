package com.example.demo.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Event")
public class EventNeo4j {

    @Id
    @GeneratedValue
    private Long id; // Neo4j usa ID come Long

    private String nome;
    private String data;

    public EventNeo4j() {
    }

    public EventNeo4j(String nome, String data) {
        this.nome = nome;
        this.data = data;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
