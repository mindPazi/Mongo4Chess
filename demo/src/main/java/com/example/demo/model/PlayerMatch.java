package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class PlayerMatch {
    ObjectId _id;
    int elo;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date date;

    public PlayerMatch(int elo, Date date, ObjectId id) {
        this._id = id;
        this.elo = elo;
        this.date = date;
    }


    public PlayerMatch() {
    }
}
