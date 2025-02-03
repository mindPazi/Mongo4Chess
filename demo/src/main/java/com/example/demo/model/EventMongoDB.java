package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "events")
public class EventMongoDB {

    @Id
    private String id; // MongoDB usa ID come String (ObjectId)

    private String nome;
    private String data;

    public EventMongoDB() {
        this.id = UUID.randomUUID().toString(); // Genera un UUID per MongoDB
    }

    public EventMongoDB(String nome, String data) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.data = data;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
