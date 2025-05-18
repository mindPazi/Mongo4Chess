package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "MatchCollection")
public class Match {
    @Id
    private String id;

    @Field(name = "date") // Nome esatto in MongoDB
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date date; // Ora verrà salvata come ISODate in MongoDB

    private String white;
    private String black;
    private String result;
    private int whiteElo;
    private int blackElo;
    private String timeControl;
    private String eco;
    private int plyCount;
    private String reason;
    private List<String> moves;

    // Costruttore completo
    public Match(String id, Date date, String white, String black, String result,
            int whiteElo, int blackElo, String timeControl, String eco,
            int plyCount, String reason, List<String> moves) {
        this.id = id;
        this.date = date;
        this.white = white;
        this.black = black;
        this.result = result;
        this.whiteElo = whiteElo;
        this.blackElo = blackElo;
        this.timeControl = timeControl;
        this.eco = eco;
        this.plyCount = plyCount;
        this.reason = reason;
        this.moves = moves;
    }

    public Match() {
    }

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Errore nella conversione di Match in JSON", e);
        }
    }

}
