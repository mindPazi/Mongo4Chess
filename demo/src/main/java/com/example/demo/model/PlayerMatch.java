package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Data;

import java.util.Date;

@Data
public class PlayerMatch {
    String _id;
    int elo;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date date;

    public PlayerMatch(int elo, Date date, String id) {
        this._id = id;
        this.elo = elo;
        this.date = date;
    }


    public PlayerMatch() {
    }
}
