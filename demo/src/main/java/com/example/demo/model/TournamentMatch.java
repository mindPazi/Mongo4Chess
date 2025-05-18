package com.example.demo.model;

import lombok.Data;

//viene salvato il match per intero e il grado, così da poter eseguire la query seeMostImportantMatchForTournament
@Data
public class TournamentMatch {
    private String matchGrade;
    private Match match;
}
