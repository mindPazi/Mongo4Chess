package com.example.demo.model;

import lombok.Data;

import java.util.Date;

@Data
public class PlayerTournament {
    String id;
    String name;
    Date startDate;
    Date endDate;
    int position;

    public PlayerTournament(String id, String name, Date startDate, Date endDate, int position) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.position = position;

    }

    public PlayerTournament(String id, int position) {
    }
}
