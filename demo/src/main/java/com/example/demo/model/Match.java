package com.example.demo.model;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "MatchCollection")

public class Match {
    @Id
    private String id; // ID della partita
    private Date date; // Data della partita
    private String white; // Nome o ID del giocatore bianco
    private String black; // Nome o ID del giocatore nero
    private String result; // Risultato della partita ('W', 'B', 'D', ecc.)

    // white elo e black elo non vanno inseriti dal giocatore che carica la partita,
    // li calcola direttamente l'applicazione
    private int whiteElo; // Elo del giocatore bianco
    private int blackElo; // Elo del giocatore nero
    private String timeControl; // Tipo di controllo del tempo (ad esempio "blitz", "classico", ecc.)
    private LocalTime time; // Tempo totale della partita
    private String ECO; // Codice ECO (per la apertura delle scacchi)
    private int plyCount; // Numero di mosse (ply)
    private String reason; // Motivo della fine della partita (ad esempio, "matto", "abbandono", ecc.)
    private List<String> moves; // Lista delle mosse in formato String

    // Costruttore
    public Match(String id, Date date, String white, String black, String result,
            String timeControl, LocalTime time,
            String ECO, int plyCount, String reason, List<String> moves) {
        this.id = id;
        this.date = date;
        this.white = white;
        this.black = black;
        this.result = result;
        // the Elo is before the match
        this.timeControl = timeControl;
        this.time = time;
        this.ECO = ECO;
        this.plyCount = plyCount;
        this.reason = reason;
        this.moves = moves;
    }

    public Match() {
    }
}
