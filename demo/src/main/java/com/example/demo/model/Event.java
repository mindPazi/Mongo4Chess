package com.example.demo.model;

public class Event {
    private String name;
    private String date;

    // Default constructor (needed by Spring)
    public Event() {
    }

    // Constructor for creating an event
    public Event(String name, String date) {
        this.name = name;
        this.date = date;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
